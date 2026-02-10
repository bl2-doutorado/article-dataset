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
import { medicalRecordApi, MedicalRecord, CreateMedicalRecordDTO } from '@/lib/api';
import { FileText, Plus, AlertCircle, Loader2 } from 'lucide-react';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { toast } from 'sonner';

export default function MedicalRecords() {
  const { user } = useAuth();
  const [records, setRecords] = useState<MedicalRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<MedicalRecord | null>(null);
  const [formData, setFormData] = useState<MedicalRecord>({
    patientId: 0,
    physicianId: user?.role === 'PHYSICIAN' ? parseInt(user!.id) : 0,
    date: format(new Date(), 'yyyy-MM-dd'),
    diagnosis: '',
    prescriptions: [],
    clinicalNotes: '',
  });
  const [prescriptionInput, setPrescriptionInput] = useState('');

  useEffect(() => {
    loadRecords();
  }, [user]);

  const loadRecords = async () => {
    try {
      setIsLoading(true);
      let response;

      if (user?.role === 'PATIENT') {
        response = await medicalRecordApi.getByPatient(parseInt(user!.id));
      } else if (user?.role === 'PHYSICIAN') {
        response = await medicalRecordApi.getByPhysician(parseInt(user!.id));
      } else {
        response = await medicalRecordApi.getAll();
      }

      setRecords(response.data);
      setError(null);
    } catch (err) {
      setError('Erro ao carregar registros');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateRecord = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.patientId || !formData.diagnosis || !formData.clinicalNotes) {
      toast.error('Preencha todos os campos obrigatórios');
      return;
    }

    try {
      await medicalRecordApi.create(formData);
      toast.success('Registro criado com sucesso');
      setIsDialogOpen(false);
      setFormData({
        patientId: 0,
        physicianId: user?.role === 'PHYSICIAN' ? parseInt(user!.id) : 0,
        date: format(new Date(), 'yyyy-MM-dd'),
        diagnosis: '',
        prescriptions: [],
        clinicalNotes: '',
      });
      setPrescriptionInput('');
      loadRecords();
    } catch (err) {
      toast.error('Erro ao criar registro');
      console.error(err);
    }
  };

  const addPrescription = () => {
    if (prescriptionInput.trim()) {
      setFormData({
        ...formData,
        prescriptions: [...formData.prescriptions, prescriptionInput],
      });
      setPrescriptionInput('');
    }
  };

  const removePrescription = (index: number) => {
    setFormData({
      ...formData,
      prescriptions: formData.prescriptions.filter((_, i) => i !== index),
    });
  };

  return (
    <Layout>
      <div className="space-y-8">
        {/* Header */}
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Registros Médicos</h1>
            <p className="text-gray-600 mt-2">
              Histórico de consultas e diagnósticos
            </p>
          </div>

          {(user?.role === 'PHYSICIAN' || user?.role === 'ADMIN') && (
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button className="flex items-center space-x-2">
                  <Plus className="h-4 w-4" />
                  <span>Novo Registro</span>
                </Button>
              </DialogTrigger>
              <DialogContent className="max-w-2xl">
                <DialogHeader>
                  <DialogTitle>Criar Registro Médico</DialogTitle>
                  <DialogDescription>
                    Preencha os dados do registro médico do paciente
                  </DialogDescription>
                </DialogHeader>

                <form onSubmit={handleCreateRecord} className="space-y-4 max-h-96 overflow-y-auto">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">ID do Paciente</label>
                    <Input
                      type="number"
                      value={formData.patientId}
                      onChange={(e) =>
                        setFormData({ ...formData, patientId: parseInt(e.target.value) })
                      }
                      placeholder="Ex: 1"
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Data</label>
                    <Input
                      type="date"
                      value={formData.date}
                      onChange={(e) =>
                        setFormData({ ...formData, date: e.target.value })
                      }
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Diagnóstico</label>
                    <Input
                      type="text"
                      value={formData.diagnosis}
                      onChange={(e) =>
                        setFormData({ ...formData, diagnosis: e.target.value })
                      }
                      placeholder="Ex: Hipertensão"
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Notas Clínicas</label>
                    <textarea
                      value={formData.clinicalNotes}
                      onChange={(e) =>
                        setFormData({ ...formData, clinicalNotes: e.target.value })
                      }
                      placeholder="Observações clínicas..."
                      className="w-full px-3 py-2 border border-gray-300 rounded-md"
                      rows={4}
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Prescrições</label>
                    <div className="flex gap-2">
                      <Input
                        type="text"
                        value={prescriptionInput}
                        onChange={(e) => setPrescriptionInput(e.target.value)}
                        placeholder="Ex: Lisinopril 10mg diariamente"
                        onKeyPress={(e) => {
                          if (e.key === 'Enter') {
                            e.preventDefault();
                            addPrescription();
                          }
                        }}
                      />
                      <Button
                        type="button"
                        variant="outline"
                        onClick={addPrescription}
                      >
                        Adicionar
                      </Button>
                    </div>

                    {formData.prescriptions.length > 0 && (
                      <div className="space-y-2 mt-2">
                        {formData.prescriptions.map((prescription, idx) => (
                          <div
                            key={idx}
                            className="flex items-center justify-between p-2 bg-gray-100 rounded"
                          >
                            <span className="text-sm">{prescription}</span>
                            <button
                              type="button"
                              onClick={() => removePrescription(idx)}
                              className="text-red-600 hover:text-red-800"
                            >
                              ✕
                            </button>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>

                  <Button type="submit" className="w-full">
                    Criar Registro
                  </Button>
                </form>
              </DialogContent>
            </Dialog>
          )}
        </div>

        {/* Records List */}
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
          </div>
        ) : error ? (
          <div className="flex items-center space-x-2 text-red-600 py-8">
            <AlertCircle className="h-5 w-5" />
            <span>{error}</span>
          </div>
        ) : records.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-12">
              <FileText className="h-12 w-12 text-gray-400 mb-2" />
              <p className="text-gray-600">Nenhum registro encontrado</p>
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-4">
            {records.map((record) => (
              <Card
                key={record.id}
                className="hover:shadow-lg transition-shadow cursor-pointer"
                onClick={() => setSelectedRecord(record)}
              >
                <CardHeader>
                  <div className="flex justify-between items-start">
                    <div>
                      <CardTitle className="text-lg">
                        {record.diagnosis}
                      </CardTitle>
                      <CardDescription>
                        Paciente #{record.patientId} • Médico #{record.physicianId}
                      </CardDescription>
                    </div>
                    <Badge variant="outline">
                      {format(new Date(record.date), 'dd/MM/yyyy', { locale: ptBR })}
                    </Badge>
                  </div>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div>
                    <h4 className="font-medium text-gray-900 mb-1">Notas Clínicas:</h4>
                    <p className="text-sm text-gray-700 line-clamp-2">
                      {record.clinicalNotes}
                    </p>
                  </div>

                  {record.prescriptions.length > 0 && (
                    <div>
                      <h4 className="font-medium text-gray-900 mb-1">Prescrições:</h4>
                      <ul className="text-sm text-gray-700 space-y-1">
                        {record.prescriptions.slice(0, 2).map((prescription, idx) => (
                          <li key={idx} className="flex items-start">
                            <span className="mr-2">•</span>
                            <span>{prescription}</span>
                          </li>
                        ))}
                        {record.prescriptions.length > 2 && (
                          <li className="text-gray-500">
                            +{record.prescriptions.length - 2} mais
                          </li>
                        )}
                      </ul>
                    </div>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>
        )}

        {/* Record Detail Dialog */}
        {selectedRecord && (
          <Dialog open={!!selectedRecord} onOpenChange={() => setSelectedRecord(null)}>
            <DialogContent className="max-w-2xl">
              <DialogHeader>
                <DialogTitle>{selectedRecord.diagnosis}</DialogTitle>
                <DialogDescription>
                  {format(new Date(selectedRecord.date), 'dd/MM/yyyy', { locale: ptBR })}
                </DialogDescription>
              </DialogHeader>

              <div className="space-y-4">
                <div>
                  <h4 className="font-medium text-gray-900 mb-2">Informações:</h4>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p className="text-gray-600">Paciente ID:</p>
                      <p className="font-medium">{selectedRecord.patientId}</p>
                    </div>
                    <div>
                      <p className="text-gray-600">Médico ID:</p>
                      <p className="font-medium">{selectedRecord.physicianId}</p>
                    </div>
                  </div>
                </div>

                <div>
                  <h4 className="font-medium text-gray-900 mb-2">Notas Clínicas:</h4>
                  <p className="text-sm text-gray-700 whitespace-pre-wrap">
                    {selectedRecord.clinicalNotes}
                  </p>
                </div>

                {selectedRecord.prescriptions.length > 0 && (
                  <div>
                    <h4 className="font-medium text-gray-900 mb-2">Prescrições:</h4>
                    <ul className="text-sm text-gray-700 space-y-1">
                      {selectedRecord.prescriptions.map((prescription, idx) => (
                        <li key={idx} className="flex items-start">
                          <span className="mr-2">•</span>
                          <span>{prescription}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </DialogContent>
          </Dialog>
        )}
      </div>
    </Layout>
  );
}
