package fr.cnes.regards.modules.jpa.test.invalid3.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.framework.jpa.annotation.InstanceEntity;

public interface InstanceRepository extends JpaRepository<InstanceEntity, Long> {

}
