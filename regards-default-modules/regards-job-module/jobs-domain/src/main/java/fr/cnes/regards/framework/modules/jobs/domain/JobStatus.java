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

/**
 * JobInfo status
 * 
 * @author Léo Mieulet
 */
public enum JobStatus {

    /**
     * Job waiting to get some resources allocated
     */
    QUEUED,
    /**
     * Job running
     */
    RUNNING,
    /**
     * Job finished without error
     */
    SUCCEEDED,
    /**
     * Job finished with error(s)
     */
    FAILED,
    /**
     * Job cancelled
     */
    ABORTED,
    /**
     * Unused state for job temporary suspended because requiring additional resources
     */
    SUSPENDED;

    @Override
    public String toString() {
        return this.name();
    }
}