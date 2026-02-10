package com.hvitops.laboratory.controller;

import com.hvitops.laboratory.entity.LabTest;
import com.hvitops.laboratory.entity.LabTestType;
import com.hvitops.laboratory.service.LabTestService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/laboratory-tests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LabTestController {

  @Inject LabTestService service;

  @POST
  public Response createLabTest(LabTest labTest) {
    labTest.setStatus("scheduled");
    LabTest created = service.createLabTest(labTest);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @GET
  @Path("/{id}")
  public Response getLabTest(@PathParam("id") String id) {
    LabTest labTest = service.getLabTestById(id);
    if (labTest == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(labTest).build();
  }

  @GET
  public Response getAllLabTests() {
    List<LabTest> tests = service.getAllLabTests();
    return Response.ok(tests).build();
  }

  @GET
  @Path("/patient/{patientId}")
  public Response getLabTestsByPatient(@PathParam("patientId") Long patientId) {
    List<LabTest> tests = service.getLabTestsByPatientId(patientId);
    return Response.ok(tests).build();
  }

  @PUT
  @Path("/{id}")
  public Response updateLabTest(@PathParam("id") String id, LabTest labTest) {
    if (labTest.getPerformedAt() != null) {
      labTest.setStatus("pending_results");
    }
    LabTest updated = service.updateLabTest(id, labTest);
    if (updated == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(updated).build();
  }

  @DELETE
  @Path("/{id}")
  public Response deleteLabTest(@PathParam("id") String id) {
    service.deleteLabTest(id);
    return Response.noContent().build();
  }

  @GET
  @Path("/types/list")
  public Response getTestTypes() {
    List<LabTestType> types = service.getTestTypes();
    return Response.ok(types).build();
  }
}
