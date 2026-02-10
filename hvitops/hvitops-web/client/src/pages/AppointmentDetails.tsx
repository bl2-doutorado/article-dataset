import React, { useState, useEffect } from 'react';
import { useParams, useLocation } from 'wouter';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { appointmentApi, medicalRecordApi, Appointment, MedicalRecord, Doctor } from '@/lib/api';
import { useAuth } from '@/contexts/AuthContext';
import {Layout}  from '@/components/Layout';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { toast } from 'sonner';
import { useState as useFormState } from 'react';

interface RouteParams {
  id: string;
}

export default function AppointmentDetails() {
  const { id } = useParams<RouteParams>();
  const [, navigate] = useLocation();
  const { user } = useAuth();
  const [appointment, setAppointment] = useState<Appointment | null>(null);
  const [medicalRecord, setMedicalRecord] = useState<MedicalRecord | null>(null);
  const [doctor, setDoctor] = useState<Doctor | null>(null);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    diagnosis: '',
    prescriptions: '',
    clinicalNotes: '',
  });

  useEffect(() => {
    loadAppointmentDetails();
  }, [id]);

  const loadAppointmentDetails = async () => {
    try {
      setLoading(true);
      const appointmentResponse = await appointmentApi.getById(parseInt(id!));
      setAppointment(appointmentResponse.data);

      if (appointmentResponse.data.medicalRecordId) {
        const recordResponse = await medicalRecordApi.getById(appointmentResponse.data.medicalRecordId);
        setMedicalRecord(recordResponse.data);
      }
    } catch (error) {
      toast.error('Erro ao carregar detalhes da consulta');
      navigate('/appointments');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveRecord = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!appointment) return;

    try {
      const prescriptionsArray = formData.prescriptions
        .split('\n')
        .map(p => p.trim())
        .filter(p => p.length > 0);

      const recordData = {
        patientId: appointment.patientId,
        physicianId: user?.id || 1,
        appointmentId: appointment.id,
        date: new Date().toISOString(),
        diagnosis: formData.diagnosis,
        prescriptions: prescriptionsArray,
        clinicalNotes: formData.clinicalNotes,
      };

      const response = await medicalRecordApi.create(recordData);
      
      const updatedAppointment = {
        ...appointment,
        medicalRecordId: response.data.id,
        status: 'COMPLETED' as const,
      };
      
      await appointmentApi.update(appointment.id, updatedAppointment);
      
      setMedicalRecord(response.data);
      setAppointment(updatedAppointment);
      setShowForm(false);
      toast.success('Registro médico criado com sucesso!');
    } catch (error) {
      toast.error('Erro ao salvar registro médico');
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex justify-center items-center min-h-screen">
          <p>Carregando...</p>
        </div>
      </Layout>
    );
  }

  if (!appointment) {
    return (
      <Layout>
        <div className="p-6">
          <p>Consulta não encontrada</p>
          <Button onClick={() => navigate('/appointments')}>Voltar</Button>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-2xl mx-auto p-6">
        <Button
          variant="ghost"
          onClick={() => navigate('/appointments')}
          className="mb-4"
        >
          ← Voltar
        </Button>

        <Card className="p-6 mb-6">
          <h1 className="text-2xl font-bold mb-4">Detalhes da Consulta</h1>
          
          <div className="space-y-4">
            <div>
              <label className="text-sm font-semibold text-gray-600">Status</label>
              <p className="text-lg">
                {appointment.status === 'SCHEDULED' ? '📅 Agendada' : '✓ Realizada'}
              </p>
            </div>

            <div>
              <label className="text-sm font-semibold text-gray-600">Data e Hora</label>
              <p className="text-lg">
                {format(new Date(appointment.scheduledAt), "dd 'de' MMMM 'de' yyyy 'às' HH:mm", { locale: ptBR })}
              </p>
            </div>

            <div>
              <label className="text-sm font-semibold text-gray-600">Médico ID</label>
              <p className="text-lg">Dr. {appointment.doctorId}</p>
            </div>

            {appointment.notes && (
              <div>
                <label className="text-sm font-semibold text-gray-600">Notas</label>
                <p className="text-lg">{appointment.notes}</p>
              </div>
            )}
          </div>
        </Card>

        {appointment.status === 'COMPLETED' && medicalRecord && (
          <Card className="p-6 mb-6">
            <h2 className="text-xl font-bold mb-4">Registro Médico</h2>
            
            <div className="space-y-4">
              <div>
                <label className="text-sm font-semibold text-gray-600">Diagnóstico</label>
                <p className="text-lg">{medicalRecord.diagnosis}</p>
              </div>

              {medicalRecord.prescriptions && medicalRecord.prescriptions.length > 0 && (
                <div>
                  <label className="text-sm font-semibold text-gray-600">Prescrições</label>
                  <ul className="list-disc list-inside">
                    {medicalRecord.prescriptions.map((p, idx) => (
                      <li key={idx}>{p}</li>
                    ))}
                  </ul>
                </div>
              )}

              <div>
                <label className="text-sm font-semibold text-gray-600">Notas Clínicas</label>
                <p className="text-lg">{medicalRecord.clinicalNotes}</p>
              </div>
            </div>
          </Card>
        )}

        {appointment.status === 'SCHEDULED' && user?.role === 'doctor' && !showForm && (
          <Button onClick={() => setShowForm(true)} className="w-full">
            Registrar Atendimento
          </Button>
        )}

        {showForm && (
          <Card className="p-6">
            <h2 className="text-xl font-bold mb-4">Registrar Atendimento</h2>
            <form onSubmit={handleSaveRecord} className="space-y-4">
              <div>
                <label className="block text-sm font-semibold mb-2">Diagnóstico</label>
                <textarea
                  value={formData.diagnosis}
                  onChange={(e) => setFormData({ ...formData, diagnosis: e.target.value })}
                  className="w-full border rounded p-2"
                  rows={3}
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-semibold mb-2">Prescrições (uma por linha)</label>
                <textarea
                  value={formData.prescriptions}
                  onChange={(e) => setFormData({ ...formData, prescriptions: e.target.value })}
                  className="w-full border rounded p-2"
                  rows={3}
                  placeholder="Medicamento 1&#10;Medicamento 2"
                />
              </div>

              <div>
                <label className="block text-sm font-semibold mb-2">Notas Clínicas</label>
                <textarea
                  value={formData.clinicalNotes}
                  onChange={(e) => setFormData({ ...formData, clinicalNotes: e.target.value })}
                  className="w-full border rounded p-2"
                  rows={3}
                  required
                />
              </div>

              <div className="flex gap-2">
                <Button type="submit" className="flex-1">Salvar</Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setShowForm(false)}
                  className="flex-1"
                >
                  Cancelar
                </Button>
              </div>
            </form>
          </Card>
        )}
      </div>
    </Layout>
  );
}
