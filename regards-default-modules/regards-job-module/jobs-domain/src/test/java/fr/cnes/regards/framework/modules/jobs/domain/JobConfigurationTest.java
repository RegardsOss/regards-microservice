/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.framework.modules.jobs.domain;

import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.OffsetDateTime;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 *
 */
public class JobConfigurationTest {

    @Test
    public void testDomain() throws URISyntaxException {
        final JobConfiguration jobConfiguration = new JobConfiguration();
        final String description = "my description";
        final OffsetDateTime now = OffsetDateTime.now();
        final JobParameters parameters = new JobParameters();
        parameters.add("param1", 17);
        jobConfiguration.setParameters(parameters);
        jobConfiguration.setExpirationDate(now);
        final String owner = "zertyuiop";
        jobConfiguration.setOwner(owner);
        final StatusInfo statusInfo = new StatusInfo();
        jobConfiguration.setStatusInfo(statusInfo);
        final int priority = 1;
        jobConfiguration.setPriority(priority);
        final String className = "fr.cnes.Class";
        jobConfiguration.setClassName(className);
        final Path workspace = FileSystems.getDefault().getPath("logs", "access.log");
        jobConfiguration.setWorkspace(workspace);
        Assertions.assertThat(jobConfiguration.getExpirationDate()).isEqualTo(now);
        Assertions.assertThat(jobConfiguration.getStatusInfo()).isEqualTo(statusInfo);
        Assertions.assertThat(jobConfiguration.getParameters()).isEqualTo(parameters);
        Assertions.assertThat(jobConfiguration.getOwner()).isEqualTo(owner);
        Assertions.assertThat(jobConfiguration.getPriority()).isEqualTo(priority);
        Assertions.assertThat(jobConfiguration.getClassName()).isEqualTo(className);
        Assertions.assertThat(jobConfiguration.getWorkspace()).isEqualTo(workspace);
        Assertions.assertThat(jobConfiguration.getParameters().getParameters()).isEqualTo(parameters.getParameters());

        final JobConfiguration jobConfiguration2 = new JobConfiguration(description, parameters, className, now, now,
                priority, workspace, owner);

        Assertions.assertThat(jobConfiguration2.getPriority()).isEqualTo(priority);
        Assertions.assertThat(jobConfiguration2.getParameters()).isEqualTo(parameters);
        Assertions.assertThat(jobConfiguration2.getWorkspace()).isEqualTo(workspace);
        Assertions.assertThat(jobConfiguration2.getClassName()).isEqualTo(className);
        Assertions.assertThat(jobConfiguration2.getOwner()).isEqualTo(owner);
    }
}