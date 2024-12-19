package org.hibernate.search.bugs;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Indexed
@Entity
@PrimaryKeyJoinColumn
public class LocalNumberPortOrder extends Order {

	private String providerOrderIdentifier;

	@GloballyIndexed
	@FullTextField(name = "localNumberPortOrder_providerOrderIdentifier")
	public String getProviderOrderIdentifier() {
		return providerOrderIdentifier;
	}

	public void setProviderOrderIdentifier(String providerOrderIdentifier) {
		this.providerOrderIdentifier = providerOrderIdentifier;
	}

}
