package com.udom.votingapplication.repositories;

import com.udom.votingapplication.models.Election;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElectionRepository extends JpaRepository<Election, Long> {
}
