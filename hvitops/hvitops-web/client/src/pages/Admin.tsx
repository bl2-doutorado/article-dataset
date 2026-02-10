import React, { useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { Layout } from '@/components/Layout';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Settings, Users, Beaker, AlertCircle } from 'lucide-react';
import { toast } from 'sonner';

export default function Admin() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('overview');

  // Dados mock de usuários
  const [users] = useState([
    { id: '1', name: 'João Silva', email: 'patient@example.com', role: 'PATIENT' },
    { id: '101', name: 'Dr. Carlos Santos', email: 'doctor@example.com', role: 'PHYSICIAN' },
    { id: '201', name: 'Maria Técnica', email: 'lab@example.com', role: 'LAB_TECHNICIAN' },
  ]);

  // Dados mock de tipos de testes
  const [testTypes] = useState([
    { id: 1, name: 'Hemograma', category: 'Hematologia' },
    { id: 2, name: 'Glicemia', category: 'Bioquímica' },
    { id: 3, name: 'Colesterol', category: 'Bioquímica' },
    { id: 4, name: 'Creatinina', category: 'Renal' },
  ]);

  // Dados mock de estatísticas
  const stats = {
    totalUsers: users.length,
    totalAppointments: 12,
    totalTests: 25,
    totalRecords: 18,
  };

  if (user?.role !== 'ADMIN') {
    return (
      <Layout>
        <div className="flex items-center justify-center py-12">
          <Card className="max-w-md">
            <CardContent className="flex flex-col items-center justify-center py-12">
              <AlertCircle className="h-12 w-12 text-red-400 mb-2" />
              <p className="text-gray-900 font-medium">Acesso Negado</p>
              <p className="text-gray-600 text-sm mt-1">
                Você não tem permissão para acessar esta página
              </p>
            </CardContent>
          </Card>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="space-y-8">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Administração</h1>
          <p className="text-gray-600 mt-2">
            Gerencie usuários, testes e configurações do sistema
          </p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600">
                Total de Usuários
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.totalUsers}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600">
                Agendamentos
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.totalAppointments}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600">
                Testes
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.totalTests}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600">
                Registros Médicos
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.totalRecords}</div>
            </CardContent>
          </Card>
        </div>

        {/* Tabs */}
        <Card>
          <CardHeader>
            <CardTitle>Gerenciamento</CardTitle>
          </CardHeader>
          <CardContent>
            <Tabs value={activeTab} onValueChange={setActiveTab}>
              <TabsList>
                <TabsTrigger value="overview" className="flex items-center space-x-2">
                  <Settings className="h-4 w-4" />
                  <span>Visão Geral</span>
                </TabsTrigger>
                <TabsTrigger value="users" className="flex items-center space-x-2">
                  <Users className="h-4 w-4" />
                  <span>Usuários</span>
                </TabsTrigger>
                <TabsTrigger value="tests" className="flex items-center space-x-2">
                  <Beaker className="h-4 w-4" />
                  <span>Tipos de Testes</span>
                </TabsTrigger>
              </TabsList>

              {/* Overview Tab */}
              <TabsContent value="overview" className="space-y-4">
                <div className="space-y-4 mt-4">
                  <div>
                    <h3 className="font-medium text-gray-900 mb-2">Atividades Recentes</h3>
                    <div className="space-y-2 text-sm text-gray-600">
                      <p>• 5 novos agendamentos criados hoje</p>
                      <p>• 3 testes concluídos</p>
                      <p>• 2 registros médicos adicionados</p>
                      <p>• 1 novo usuário registrado</p>
                    </div>
                  </div>

                  <div>
                    <h3 className="font-medium text-gray-900 mb-2">Configurações do Sistema</h3>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between items-center p-2 bg-gray-50 rounded">
                        <span className="text-gray-700">Modo de Manutenção</span>
                        <Badge variant="outline">Desativado</Badge>
                      </div>
                      <div className="flex justify-between items-center p-2 bg-gray-50 rounded">
                        <span className="text-gray-700">Backup Automático</span>
                        <Badge className="bg-green-100 text-green-800">Ativo</Badge>
                      </div>
                      <div className="flex justify-between items-center p-2 bg-gray-50 rounded">
                        <span className="text-gray-700">Notificações</span>
                        <Badge className="bg-green-100 text-green-800">Ativo</Badge>
                      </div>
                    </div>
                  </div>
                </div>
              </TabsContent>

              {/* Users Tab */}
              <TabsContent value="users" className="space-y-4">
                <div className="mt-4 overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="border-b border-gray-200">
                        <th className="text-left py-3 px-4 font-medium text-gray-700">Nome</th>
                        <th className="text-left py-3 px-4 font-medium text-gray-700">Email</th>
                        <th className="text-left py-3 px-4 font-medium text-gray-700">Função</th>
                        <th className="text-left py-3 px-4 font-medium text-gray-700">Ações</th>
                      </tr>
                    </thead>
                    <tbody>
                      {users.map((u) => (
                        <tr key={u.id} className="border-b border-gray-100 hover:bg-gray-50">
                          <td className="py-3 px-4">{u.name}</td>
                          <td className="py-3 px-4 text-sm text-gray-600">{u.email}</td>
                          <td className="py-3 px-4">
                            <Badge variant="outline">
                              {u.role === 'PATIENT' && 'Paciente'}
                              {u.role === 'PHYSICIAN' && 'Médico'}
                              {u.role === 'LAB_TECHNICIAN' && 'Técnico'}
                            </Badge>
                          </td>
                          <td className="py-3 px-4">
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => toast.info(`Editar usuário ${u.name}`)}
                            >
                              Editar
                            </Button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </TabsContent>

              {/* Tests Tab */}
              <TabsContent value="tests" className="space-y-4">
                <div className="mt-4 space-y-4">
                  {testTypes.map((test) => (
                    <div
                      key={test.id}
                      className="flex justify-between items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50"
                    >
                      <div>
                        <h4 className="font-medium text-gray-900">{test.name}</h4>
                        <p className="text-sm text-gray-600">{test.category}</p>
                      </div>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => toast.info(`Editar teste ${test.name}`)}
                      >
                        Editar
                      </Button>
                    </div>
                  ))}

                  <Button
                    variant="outline"
                    className="w-full mt-4"
                    onClick={() => toast.info('Adicionar novo tipo de teste')}
                  >
                    + Adicionar Tipo de Teste
                  </Button>
                </div>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>
      </div>
    </Layout>
  );
}
