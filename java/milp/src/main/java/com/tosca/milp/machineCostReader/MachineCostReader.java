package com.tosca.milp.machineCostReader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.tosca.milp.domain.MachineDetails;

public class MachineCostReader {
  
  public List<MachineDetails> readFile(String csvFile) throws IOException {
    try (Reader reader = Files.newBufferedReader(Paths.get(csvFile))) {
      CsvToBean<MachineDetails> cb = new CsvToBeanBuilder<MachineDetails>(reader)
        .withType(MachineDetails.class)
        .build();
      return cb.parse();
    }
  }
}
