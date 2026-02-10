package com.tosca.milp.domain;

import com.opencsv.bean.CsvBindByName;

public class MachineDetails {

  @CsvBindByName(column = "Provider")
  // @CsvBindByPosition(position = 0)
  private String provider;

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  // @CsvBindByPosition(position = 1)
  @CsvBindByName(column = "Model")
  private String name;

  // @CsvBindByPosition(position = 2)
  @CsvBindByName(column = "RAM")
  private Long memory;

  // @CsvBindByPosition(position = 3)
  @CsvBindByName(column = "vCPUs")
  private Long vcpu;

  // @CsvBindByPosition(position = 4)
  @CsvBindByName(column = "Price")
  private Double price;

  // @CsvBindByPosition(position = 5)
  @CsvBindByName(column = "CarbonFootprint")
  private Double carbonFootprint;

  public Double getCarbonFootprint() {
    return carbonFootprint;
  }

  public void setCarbonFootprint(Double carbonFootprint) {
    this.carbonFootprint = carbonFootprint;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public void setMemory(String memory) {
    this.memory = Long.parseLong(memory);
  }

  public Long getMemory() {
    return this.memory;
  }

  public void setVCPU(String vcpu) {
    this.vcpu = Long.parseLong(vcpu);
  }

  public Long getVCPU() {
    return this.vcpu;
  }

  public void setPrice(String price) {
    this.price = Double.parseDouble(price);
  }

  public Double getPrice() {
    return this.price;
  }
}
