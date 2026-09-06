package com.rental.entity;

import com.rental.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "amenities")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Amenity extends BaseEntity {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(unique = true, nullable = false)
    private String amenityName;

    private String icon;
    private String category;

    // ✅ Many-to-Many with Property
    @ManyToMany(mappedBy = "amenities")
    @Builder.Default
    private List<Property> properties = new ArrayList<>();
}