package com.rental.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "properties")
public class Property {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	private String description;
	private String location;
	private Double rent;
	private Integer bedrooms;
	private String contactNumber;
	private Boolean available;
	@ElementCollection
	@CollectionTable(name = "property_image_urls", joinColumns = @JoinColumn(name = "property_id"))
	@Column(name = "image_url")
	private List<String> imageUrls = new ArrayList<>();
	private Long ownerId;
	private LocalDateTime createdAt;
}