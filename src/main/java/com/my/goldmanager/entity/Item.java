package com.my.goldmanager.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ITEM")
public class Item {

	@Id	
	@Getter
	@Setter
	private String id;

	@Setter
	@Getter
	@Column
	private String name;

	@Setter
	@Getter
	@Column
	private float amount;

	@ManyToOne
	@JoinColumn(name = "itemtype")
	@Setter
	@Getter
	private ItemType itemType;
	

	@ManyToOne
	@JoinColumn(name = "unit")
	@Setter
	@Getter
	private Unit unit;

	@PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
