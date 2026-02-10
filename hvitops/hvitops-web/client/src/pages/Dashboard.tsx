import React, { useState, useEffect } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { Layout } from '@/components/Layout';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { appointmentApi, Appointment } from '@/lib/api';
import { Calendar, Clock, AlertCircle, Loader2 } from 'lucide-react';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';

export default function Dashboard() {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadAppointments = async () => {
      try {
        setIsLoading(true);
        let response;

        if (user?.role === 'PATIENT') {
          response = await appointmentApi.getByPatient(parseInt(user.id));
        } else if (user?.role === 'PHYSICIAN') {
          response = await appointmentApi.getByDoctor(parseInt(user.id));
        } else {
          response = await appointmentApi.getAll();
        }

        setAppointments(response.data);
      } catch (err) {
        setError('Erro ao carregar agendamentos');
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    loadAppointments();
  }, [user]);

  const upcomingAppointments = appointments
    .filter(apt => apt.status === 'SCHEDULED')
    .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime())
    .slice(0, 5);

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
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Bem-vindo, {user?.name}!
          </h1>
          <p className="text-gray-600 mt-2">
            Aqui está um resumo de suas atividades
          </p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600">
                Total de Agendamentos
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{appointments.length}</div>
              <p className="text-xs text-gray-500 mt-1">
                {appointments.filter(a => a.status === 'SCHEDULED').length} agendados
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600">
                Próximos Agendamentos
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{upcomingAppointments.length}</div>
              <p className="text-xs text-gray-500 mt-1">
                Nos próximos 30 dias
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600">
                Concluídos
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {appointments.filter(a => a.status === 'COMPLETED').length}
              </div>
              <p className="text-xs text-gray-500 mt-1">
                Consultas realizadas
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Upcoming Appointments */}
        <Card>
          <CardHeader>
            <CardTitle>Próximos Agendamentos</CardTitle>
            <CardDescription>
              Seus agendamentos programados
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
            ) : upcomingAppointments.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <Calendar className="h-12 w-12 mx-auto mb-2 opacity-50" />
                <p>Nenhum agendamento programado</p>
              </div>
            ) : (
              <div className="space-y-4">
                {upcomingAppointments.map((apt) => (
                  <div
                    key={apt.id}
                    className="flex items-start space-x-4 p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    <div className="flex-shrink-0">
                      <div className="flex items-center justify-center h-10 w-10 rounded-full bg-blue-100">
                        <Clock className="h-5 w-5 text-blue-600" />
                      </div>
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-gray-900">
                        {user?.role === 'PATIENT'
                          ? `Consulta com Médico #${apt.doctorId}`
                          : `Consulta com Paciente #${apt.patientId}`}
                      </p>
                      <p className="text-sm text-gray-500 mt-1">
                        {format(
                          new Date(apt.scheduledAt),
                          "EEEE, d 'de' MMMM 'às' HH:mm",
                          { locale: ptBR }
                        )}
                      </p>
                      {apt.notes && (
                        <p className="text-sm text-gray-600 mt-2 italic">
                          Notas: {apt.notes}
                        </p>
                      )}
                    </div>
                    <div className="flex-shrink-0">
                      {getStatusBadge(apt.status)}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Quick Actions */}
        <Card>
          <CardHeader>
            <CardTitle>Ações Rápidas</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {user?.role === 'PATIENT' && (
                <>
                  <a
                    href="/appointments"
                    className="p-4 border border-gray-200 rounded-lg hover:bg-blue-50 transition-colors"
                  >
                    <h3 className="font-medium text-gray-900">Agendar Consulta</h3>
                    <p className="text-sm text-gray-600 mt-1">
                      Marque uma nova consulta com um médico
                    </p>
                  </a>
                  <a
                    href="/lab-tests"
                    className="p-4 border border-gray-200 rounded-lg hover:bg-blue-50 transition-colors"
                  >
                    <h3 className="font-medium text-gray-900">Solicitar Teste</h3>
                    <p className="text-sm text-gray-600 mt-1">
                      Solicite um novo teste laboratorial
                    </p>
                  </a>
                </>
              )}
              {user?.role === 'PHYSICIAN' && (
                <>
                  <a
                    href="/medical-records"
                    className="p-4 border border-gray-200 rounded-lg hover:bg-blue-50 transition-colors"
                  >
                    <h3 className="font-medium text-gray-900">Registros Médicos</h3>
                    <p className="text-sm text-gray-600 mt-1">
                      Visualizar e criar registros de pacientes
                    </p>
                  </a>
                  <a
                    href="/appointments"
                    className="p-4 border border-gray-200 rounded-lg hover:bg-blue-50 transition-colors"
                  >
                    <h3 className="font-medium text-gray-900">Meus Agendamentos</h3>
                    <p className="text-sm text-gray-600 mt-1">
                      Gerenciar consultas agendadas
                    </p>
                  </a>
                </>
              )}
              {user?.role === 'LAB_TECHNICIAN' && (
                <a
                  href="/lab-tests"
                  className="p-4 border border-gray-200 rounded-lg hover:bg-blue-50 transition-colors"
                >
                  <h3 className="font-medium text-gray-900">Testes Laboratoriais</h3>
                  <p className="text-sm text-gray-600 mt-1">
                    Atualizar status e resultados de testes
                  </p>
                </a>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </Layout>
  );
}
