package com.calisthenics.calisthenics.repository;

/*Import library for Repository*/
import com.calisthenics.calisthenics.model.BodyRegion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodyRegionRepository extends JpaRepository<BodyRegion, Long> {
}
