package com.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workitems")
    private String workitems;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "prices")
    private Double prices;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWorkitems() { return workitems; }
    public void setWorkitems(String workitems) { this.workitems = workitems; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPrices() { return prices; }
    public void setPrices(Double prices) { this.prices = prices; }
}