package com.rental.entity;

import com.rental.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "property_types")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PropertyType extends BaseEntity {
	private static final long serialVersionUID = 1L;
    @Column(unique = true, nullable = false)
    private String typeName;

    private String icon;

    // ✅ Relationship with Property
    @OneToMany(mappedBy = "propertyType")
    @Builder.Default
    private List<Property> properties = new ArrayList<>();
}