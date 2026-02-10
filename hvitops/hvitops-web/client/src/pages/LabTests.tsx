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
import { labTestApi, LabTest, LabTestType, CreateLabTestDTO, LabTestItem } from '@/lib/api';
import { Beaker, Plus, AlertCircle, Loader2, Eye, CheckCircle } from 'lucide-react';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { toast } from 'sonner';

export default function LabTests() {
  const { user } = useAuth();
  const [labTests, setLabTests] = useState<LabTest[]>([]);
  const [testTypes, setTestTypes] = useState<LabTestType[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [selectedTestId, setSelectedTestId] = useState<string | null>(null);
  const [selectedTest, setSelectedTest] = useState<LabTest | null>(null);
  const [formData, setFormData] = useState<CreateLabTestDTO>({
    patientId: user?.role === 'PATIENT' ? parseInt(user.id) : undefined,
    date: format(new Date(), "yyyy-MM-dd'T'HH:mm:ss"),
    items: [],
  });
  const [selectedTestType, setSelectedTestType] = useState('');

  useEffect(() => {
    loadLabTests();
    loadTestTypes();
  }, [user]);

  const loadLabTests = async () => {
    try {
      setIsLoading(true);
      let response;

      if (user?.role === 'PATIENT') {
        response = await labTestApi.getByPatient(parseInt(user!.id));
      } else if (user?.role === 'LAB_TECHNICIAN') {
        response = await labTestApi.getAll();
      } else {
        response = await labTestApi.getAll();
      }

      setLabTests(response.data);
      setError(null);
    } catch (err) {
      setError('Erro ao carregar testes laboratoriais');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const loadTestTypes = async () => {
    try {
      const response = await labTestApi.getTestTypes();
      setTestTypes(response.data);
    } catch (err) {
      console.error('Erro ao carregar tipos de testes:', err);
    }
  };

  const handleAddTestItem = () => {
    if (!selectedTestType) {
      toast.error('Selecione um tipo de teste');
      return;
    }

    const newItem: LabTestItem = {
      testType: selectedTestType,
    };

    setFormData({
      ...formData,
      items: [...formData.items, newItem],
    });

    setSelectedTestType('');
  };

  const handleRemoveTestItem = (index: number) => {
    setFormData({
      ...formData,
      items: formData.items.filter((_, i) => i !== index),
    });
  };

  const handleCreateLabTest = async (e: React.FormEvent) => {
    e.preventDefault();

    if (formData.items.length === 0) {
      toast.error('Adicione pelo menos um tipo de teste');
      return;
    }

    try {
      await labTestApi.create(formData);
      toast.success('Solicitação de teste criada com sucesso');
      setIsDialogOpen(false);
      setFormData({
        patientId: user?.role === 'PATIENT' ? parseInt(user.id) : undefined,
        date: format(new Date(), "yyyy-MM-dd'T'HH:mm:ss"),
        items: [],
      });
      loadLabTests();
    } catch (err) {
      toast.error('Erro ao criar solicitação de teste');
      console.error(err);
    }
  };

  const handleConfirmTest = async (testId: string) => {
    try {
      const performedAt = format(new Date(), "yyyy-MM-dd'T'HH:mm:ss");
      await labTestApi.update(testId, {
        status: 'pending_results',
        performedAt,
      });
      toast.success('Teste marcado como realizado');
      loadLabTests();
    } catch (err) {
      toast.error('Erro ao atualizar teste');
      console.error(err);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'scheduled':
        return <Badge className="bg-blue-100 text-blue-800">Agendado</Badge>;
      case 'pending_results':
        return <Badge className="bg-yellow-100 text-yellow-800">Aguardando Resultado</Badge>;
      case 'completed':
        return <Badge className="bg-green-100 text-green-800">Concluído</Badge>;
      default:
        return <Badge>{status}</Badge>;
    }
  };

  const filteredTests = user?.role === 'LAB_TECHNICIAN'
    ? labTests.filter(t => t.status === 'scheduled' || t.status === 'pending_results')
    : labTests;

  return (
    <Layout>
      <div className="space-y-8">
        {/* Header */}
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Testes Laboratoriais</h1>
            <p className="text-gray-600 mt-2">
              {user?.role === 'LAB_TECHNICIAN'
                ? 'Gerencie os testes laboratoriais'
                : 'Solicite e acompanhe seus testes'}
            </p>
          </div>

          {user?.role === 'PATIENT' && (
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button className="flex items-center space-x-2">
                  <Plus className="h-4 w-4" />
                  <span>Solicitar Teste</span>
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Solicitar Teste Laboratorial</DialogTitle>
                  <DialogDescription>
                    Selecione os testes que deseja realizar
                  </DialogDescription>
                </DialogHeader>

                <form onSubmit={handleCreateLabTest} className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Data do Teste</label>
                    <Input
                      type="datetime-local"
                      value={formData.date}
                      onChange={(e) =>
                        setFormData({ ...formData, date: e.target.value })
                      }
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Tipo de Teste</label>
                    <div className="flex gap-2">
                      <Select value={selectedTestType} onValueChange={setSelectedTestType}>
                        <SelectTrigger>
                          <SelectValue placeholder="Selecione um teste" />
                        </SelectTrigger>
                        <SelectContent>
                          {testTypes?.map((type) => (
                            <SelectItem key={type.id} value={type.id}>
                              {type.name}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <Button type="button" variant="outline" onClick={handleAddTestItem}>
                        Adicionar
                      </Button>
                    </div>
                  </div>

                  {formData.items.length > 0 && (
                    <div className="space-y-2">
                      <label className="text-sm font-medium">Testes Selecionados</label>
                      <div className="space-y-2">
                        {formData.items?.map((item, idx) => (
                          <div key={idx} className="flex justify-between items-center bg-gray-100 p-2 rounded">
                            <span>{item.testType}</span>
                            <Button
                              type="button"
                              variant="ghost"
                              size="sm"
                              onClick={() => handleRemoveTestItem(idx)}
                            >
                              ✕
                            </Button>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  <Button type="submit" className="w-full">
                    Solicitar Testes
                  </Button>
                </form>
              </DialogContent>
            </Dialog>
          )}
        </div>

        {/* Tests List */}
        <Card>
          <CardHeader>
            <CardTitle>
              {user?.role === 'LAB_TECHNICIAN' ? 'Testes a Realizar' : 'Meus Testes'}
            </CardTitle>
            <CardDescription>
              Total: {filteredTests.length} testes
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
            ) : filteredTests.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <Beaker className="h-12 w-12 mx-auto mb-2 opacity-50" />
                <p>Nenhum teste encontrado</p>
              </div>
            ) : (
              <div className="grid gap-4">
                {filteredTests?.map((test) => (
                  <Card key={test.id} className="border">
                    <CardContent className="pt-6">
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-2">
                            <h3 className="font-semibold">Teste #{test.id}</h3>
                            {getStatusBadge(test.status)}
                          </div>
                          <p className="text-sm text-gray-600">
                            Data: {format(new Date(test.date), "dd/MM/yyyy'T'HH:mm:ss", { locale: ptBR })}
                          </p>
                          <p className="text-sm text-gray-600">
                            Testes: {test?.items?.map(i => i.testType).join(', ')}
                          </p>
                        </div>
                        <div className="flex gap-2">
                          {user?.role === 'LAB_TECHNICIAN' && test.status === 'scheduled' && (
                            <Button
                              size="sm"
                              onClick={() => handleConfirmTest(test.id)}
                              className="flex items-center gap-1"
                            >
                              <CheckCircle className="h-4 w-4" />
                              Confirmar Realização
                            </Button>
                          )}
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </Layout>
  );
}
