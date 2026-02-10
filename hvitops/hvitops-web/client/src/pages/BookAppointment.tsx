import React, { useState, useEffect } from 'react';
import { useLocation } from 'wouter';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { appointmentApi, Doctor, AvailableSlot } from '@/lib/api';
import { useAuth } from '@/contexts/AuthContext';
import {Layout} from '@/components/Layout';
import { format, addDays, startOfWeek, isMonday } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { toast } from 'sonner';

export default function BookAppointment() {
  const [, navigate] = useLocation();
  const { user } = useAuth();
  const [step, setStep] = useState<'specialty' | 'doctor' | 'slot'>('specialty');
  const [specialties, setSpecialties] = useState<string[]>([]);
  const [selectedSpecialty, setSelectedSpecialty] = useState('');
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [selectedDoctor, setSelectedDoctor] = useState<Doctor | null>(null);
  const [slots, setSlots] = useState<AvailableSlot[]>([]);
  const [selectedSlot, setSelectedSlot] = useState<AvailableSlot | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadSpecialties();
  }, []);

  const loadSpecialties = async () => {
    try {
      const response = await appointmentApi.getSpecialties();
      setSpecialties(response.data);
    } catch (error) {
      toast.error('Erro ao carregar especialidades');
    }
  };

  const handleSpecialtySelect = async (specialty: string) => {
    setSelectedSpecialty(specialty);
    setLoading(true);
    try {
      const response = await appointmentApi.getDoctorsBySpecialty(specialty);
      setDoctors(response.data);
      setStep('doctor');
    } catch (error) {
      toast.error('Erro ao carregar médicos');
    } finally {
      setLoading(false);
    }
  };

  const handleDoctorSelect = async (doctor: Doctor) => {
    setSelectedDoctor(doctor);
    setLoading(true);
    try {
      const startDate = startOfWeek(new Date(), { weekStartsOn: 1 });
      const endDate = addDays(startDate, 30);
      
      const response = await appointmentApi.getAvailableSlots(
        doctor.id,
        format(startDate, 'yyyy-MM-dd'),
        format(endDate, 'yyyy-MM-dd')
      );
      setSlots(response.data);
      setStep('slot');
    } catch (error) {
      toast.error('Erro ao carregar horários disponíveis');
    } finally {
      setLoading(false);
    }
  };

  const handleSlotSelect = async (slot: AvailableSlot) => {
    if (!slot.available) return;
    
    setLoading(true);
    try {
      await appointmentApi.create({
        patientId: user?.id || 1,
        doctorId: selectedDoctor!.id,
        scheduledAt: slot.startTime,
        status: 'SCHEDULED',
      });
      toast.success('Consulta agendada com sucesso!');
      navigate('/appointments');
    } catch (error) {
      toast.error('Erro ao agendar consulta');
    } finally {
      setLoading(false);
    }
  };

  const groupedSlots = slots.reduce((acc, slot) => {
    const date = format(new Date(slot.startTime), 'yyyy-MM-dd');
    if (!acc[date]) acc[date] = [];
    acc[date].push(slot);
    return acc;
  }, {} as Record<string, AvailableSlot[]>);

  return (
    <Layout>
      <div className="max-w-2xl mx-auto p-6">
        <h1 className="text-3xl font-bold mb-8">Agendar Consulta</h1>

        {step === 'specialty' && (
          <Card className="p-6">
            <h2 className="text-xl font-semibold mb-4">Selecione uma Especialidade</h2>
            <div className="grid grid-cols-2 gap-3">
              {specialties.map((specialty) => (
                <Button
                  key={specialty}
                  variant="outline"
                  onClick={() => handleSpecialtySelect(specialty)}
                  disabled={loading}
                  className="h-auto py-3"
                >
                  {specialty}
                </Button>
              ))}
            </div>
          </Card>
        )}

        {step === 'doctor' && (
          <Card className="p-6">
            <Button
              variant="ghost"
              onClick={() => setStep('specialty')}
              className="mb-4"
            >
              ← Voltar
            </Button>
            <h2 className="text-xl font-semibold mb-4">Selecione um Médico</h2>
            <div className="space-y-3">
              {doctors.map((doctor) => (
                <Button
                  key={doctor.id}
                  variant="outline"
                  onClick={() => handleDoctorSelect(doctor)}
                  disabled={loading}
                  className="w-full text-left justify-start h-auto py-3"
                >
                  <div>
                    <div className="font-semibold">{doctor.name}</div>
                    <div className="text-sm text-gray-600">{doctor.specialty}</div>
                  </div>
                </Button>
              ))}
            </div>
          </Card>
        )}

        {step === 'slot' && (
          <Card className="p-6">
            <Button
              variant="ghost"
              onClick={() => setStep('doctor')}
              className="mb-4"
            >
              ← Voltar
            </Button>
            <h2 className="text-xl font-semibold mb-4">Selecione um Horário</h2>
            <div className="space-y-6">
              {Object.entries(groupedSlots).map(([date, daySlots]) => (
                <div key={date}>
                  <h3 className="font-semibold mb-3">
                    {format(new Date(date), 'EEEE, dd MMMM', { locale: ptBR })}
                  </h3>
                  <div className="grid grid-cols-4 gap-2">
                    {daySlots.map((slot, idx) => (
                      <Button
                        key={idx}
                        variant={slot.available ? 'outline' : 'ghost'}
                        disabled={!slot.available || loading}
                        onClick={() => handleSlotSelect(slot)}
                        className="h-auto py-2"
                      >
                        {format(new Date(slot.startTime), 'HH:mm')}
                      </Button>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </Card>
        )}
      </div>
    </Layout>
  );
}
