package com.rhbgroup.dcpbo.customer.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.customer.model.CardProfile;

@Repository
public interface CardRepository extends CrudRepository<CardProfile, Integer> {
	public CardProfile findById(Integer id);
}
