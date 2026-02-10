import React, { useState, useEffect } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { Layout } from '@/components/Layout';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { appointmentApi, Appointment, CreateAppointmentDTO, Doctor } from '@/lib/api';
import { Calendar, Plus, Trash2, AlertCircle, Loader2, Eye } from 'lucide-react';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { toast } from 'sonner';
import { useLocation } from 'wouter';

export default function Appointments() {
  const { user } = useAuth();
  const [location, navigate] = useLocation();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [formData, setFormData] = useState<CreateAppointmentDTO>({
    patientId: user?.role === 'PATIENT' ? parseInt(user.id) : undefined,
    doctorId: 0,
    scheduledAt: '',
    notes: '',
  });

  useEffect(() => {
    loadAppointments();
    if (user?.role === 'PATIENT') {
      loadDoctors();
    }
  }, [user]);

  const loadAppointments = async () => {
    try {
      setIsLoading(true);
      let response;

      if (user?.role === 'PATIENT') {
        response = await appointmentApi.getByPatient(parseInt(user!.id));
      } else if (user?.role === 'PHYSICIAN') {
        response = await appointmentApi.getByDoctor(parseInt(user!.id));
      } else {
        response = await appointmentApi.getAll();
      }

      setAppointments(response.data);
      setError(null);
    } catch (err) {
      setError('Erro ao carregar agendamentos');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const loadDoctors = async () => {
    try {
      const response = await appointmentApi.getDoctorsBySpecialty();
      setDoctors(response.data);
    } catch (err) {
      console.error('Erro ao carregar médicos:', err);
      toast.error('Erro ao carregar lista de médicos');
    }
  };

  const handleCreateAppointment = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.scheduledAt || formData.doctorId === 0) {
      toast.error('Selecione um médico e uma data/hora');
      return;
    }

    if (new Date(formData.scheduledAt) < new Date()) {
      toast.error('Não é permitido agendar no passado');
      return;
    }

    try {
      await appointmentApi.create(formData);
      toast.success('Agendamento criado com sucesso');
      setIsDialogOpen(false);
      setFormData({
        patientId: user?.role === 'PATIENT' ? parseInt(user.id) : undefined,
        doctorId: 0,
        scheduledAt: '',
        notes: '',
      });
      loadAppointments();
    } catch (err) {
      toast.error('Erro ao criar agendamento');
      console.error(err);
    }
  };

  const handleDeleteAppointment = async (id: number) => {
    if (!confirm('Tem certeza que deseja cancelar este agendamento?')) return;

    try {
      await appointmentApi.cancel(id);
      toast.success('Agendamento cancelado');
      loadAppointments();
    } catch (err) {
      toast.error('Erro ao cancelar agendamento');
      console.error(err);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'SCHEDULED':
        return <Badge className="bg-blue-100 text-blue-800">Agendado</Badge>;
      case 'COMPLETED':
        return <Badge className="bg-green-100 text-green-800">Concluído</Badge>;
      case 'CANCELLED':
        return <Badge className="bg-red-100 text-red-800">Cancelado</Badge>;
      default:
        return <Badge>{status}</Badge>;
    }
  };

  return (
    <Layout>
      <div className="space-y-8">
        {/* Header */}
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Agendamentos</h1>
            <p className="text-gray-600 mt-2">
              Gerencie suas consultas e agendamentos
            </p>
          </div>

          {user?.role === 'PATIENT' && (
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button className="flex items-center space-x-2">
                  <Plus className="h-4 w-4" />
                  <span>Nova Consulta</span>
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Agendar Consulta</DialogTitle>
                  <DialogDescription>
                    Preencha os dados para agendar uma nova consulta
                  </DialogDescription>
                </DialogHeader>

                <form onSubmit={handleCreateAppointment} className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Médico</label>
                    <Select
                      value={formData.doctorId.toString()}
                      onValueChange={(value) =>
                        setFormData({ ...formData, doctorId: parseInt(value) })
                      }
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Selecione um médico" />
                      </SelectTrigger>
                      <SelectContent>
                        {doctors.map((doctor) => (
                          <SelectItem key={doctor.id} value={doctor.id.toString()}>
                            {doctor.name} - {doctor.specialty}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Data e Hora</label>
                    <Input
                      type="datetime-local"
                      value={formData.scheduledAt}
                      onChange={(e) =>
                        setFormData({ ...formData, scheduledAt: e.target.value })
                      }
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Notas (opcional)</label>
                    <Input
                      type="text"
                      value={formData.notes}
                      onChange={(e) =>
                        setFormData({ ...formData, notes: e.target.value })
                      }
                      placeholder="Ex: Consulta de rotina"
                    />
                  </div>

                  <Button type="submit" className="w-full">
                    Agendar
                  </Button>
                </form>
              </DialogContent>
            </Dialog>
          )}
        </div>

        {/* Appointments List */}
        <Card>
          <CardHeader>
            <CardTitle>Lista de Agendamentos</CardTitle>
            <CardDescription>
              Total: {appointments.length} agendamentos
            </CardDescription>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
              </div>
            ) : error ? (
              <div className="flex items-center space-x-2 text-red-600 py-8">
                <AlertCircle className="h-5 w-5" />
                <span>{error}</span>
              </div>
            ) : appointments.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <Calendar className="h-12 w-12 mx-auto mb-2 opacity-50" />
                <p>Nenhum agendamento encontrado</p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-gray-200">
                      <th className="text-left py-3 px-4 font-medium text-gray-700">
                        Data/Hora
                      </th>
                      <th className="text-left py-3 px-4 font-medium text-gray-700">
                        {user?.role === 'PATIENT' ? 'Médico' : 'Paciente'}
                      </th>
                      <th className="text-left py-3 px-4 font-medium text-gray-700">
                        Status
                      </th>
                      <th className="text-left py-3 px-4 font-medium text-gray-700">
                        Notas
                      </th>
                      <th className="text-left py-3 px-4 font-medium text-gray-700">
                        Ações
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {appointments.map((apt) => (
                      <tr key={apt.id} className="border-b border-gray-100 hover:bg-gray-50">
                        <td className="py-3 px-4">
                          {format(
                            new Date(apt.scheduledAt),
                            "dd/MM/yyyy HH:mm",
                            { locale: ptBR }
                          )}
                        </td>
                        <td className="py-3 px-4">
                          {user?.role === 'PATIENT'
                            ? `Médico #${apt.doctorId}`
                            : `Paciente #${apt.patientId}`}
                        </td>
                        <td className="py-3 px-4">
                          {getStatusBadge(apt.status)}
                        </td>
                        <td className="py-3 px-4 text-sm text-gray-600">
                          {apt.notes || '-'}
                        </td>
                        <td className="py-3 px-4 flex gap-2">
                          <button
                            onClick={() => navigate(`/appointments/${apt.id}`)}
                            className="text-blue-600 hover:text-blue-800 transition-colors"
                            title="Ver detalhes"
                          >
                            <Eye className="h-4 w-4" />
                          </button>
                          {apt.status === 'SCHEDULED' && (
                            <button
                              onClick={() => handleDeleteAppointment(apt.id)}
                              className="text-red-600 hover:text-red-800 transition-colors"
                              title="Cancelar"
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </Layout>
  );
}
