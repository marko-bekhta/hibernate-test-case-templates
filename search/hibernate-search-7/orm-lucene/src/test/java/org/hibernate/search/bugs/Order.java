package org.hibernate.search.bugs;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "Order_")
public abstract class Order {

	@Id
	private Long id;

	private String tags;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@GloballyIndexed
	@FullTextField(name = "order_tags")
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

}
