package com.traveltrek.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Entity
@Table(name = "travel_package")
public class TravelPackage {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(nullable=false)
    private String packageName;
    @NotBlank
    @Column(nullable=false)
    private String destination;
    @NotNull
    @Positive 
    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal basePrice;
    @NotNull
    @Column(nullable=false)
    private Integer availableSlots; 
    public TravelPackage()
     {}
        public TravelPackage(String packageName,String destination,BigDecimal basePrice,Integer availableSlots) {
        this.packageName = packageName;
        this.destination = destination;
        this.basePrice = basePrice;
        this.availableSlots = availableSlots;
    }

    // --- Getters and Setters ---

    public Long getId() { 
        return id;
        }

    public String getPackageName() {
         return packageName; 
        }
    public void setPackageName(String packageName) { 
        this.packageName = packageName; 
        }

    public String getDestination() { 
        return destination;
    
         }
    public void setDestination(String destination) { 
        this.destination = destination; 
         }

    public BigDecimal getBasePrice() {
         return basePrice;
        }
    public void setBasePrice(BigDecimal basePrice) {
         this.basePrice = basePrice; 
        }

    public Integer getAvailableSlots() {
         return availableSlots; 
        }
    public void setAvailableSlots(Integer availableSlots) {
         this.availableSlots = availableSlots;
         }
}
