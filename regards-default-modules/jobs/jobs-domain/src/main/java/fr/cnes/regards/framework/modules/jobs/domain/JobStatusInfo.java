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

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.annotations.Type;

import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;

/**
 * Store job status
 * @author Léo Mieulet
 * @author Christophe Mertz
 */
@Embeddable
public class JobStatusInfo implements Observer {

    /**
     * Job status
     */
    @Column(name = "status", length = 16)
    @Enumerated(value = EnumType.STRING)
    private JobStatus status;

    /**
     * Date of current status update
     */
    @Column(name = "statusDate")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime statusDate;

    /**
     * Estimated date to job completion
     */
    @Column(name = "estimatedCompletion")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime estimatedCompletion;

    /**
     * the job advancement
     */
    @Column(name = "percentCompleted")
    private int percentCompleted;

    /**
     * the job creation date
     */
    @Column(name = "startDate")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime startDate;

    /**
     * the job end date
     */
    @Column(name = "stopDate")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime stopDate;

    /**
     * In case of error, contains the stack trace
     */
    @Column(name = "stacktrace")
    @Type(type = "text")
    private String stackTrace;

    @Transient
    private AtomicBoolean completionChanged = new AtomicBoolean(false);

    public JobStatusInfo() {
    }

    public OffsetDateTime getEstimatedCompletion() {
        return estimatedCompletion;
    }

    public void setEstimatedCompletion(final OffsetDateTime pEstimatedCompletion) {
        estimatedCompletion = pEstimatedCompletion;
    }

    public int getPercentCompleted() {
        return percentCompleted;
    }

    public void setPercentCompleted(final int pPercentCompleted) {
        percentCompleted = pPercentCompleted;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(final OffsetDateTime pStartDate) {
        startDate = pStartDate;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(final JobStatus pStatus) {
        status = pStatus;
        statusDate = OffsetDateTime.now();
    }

    public OffsetDateTime getStopDate() {
        return stopDate;
    }

    public void setStopDate(final OffsetDateTime pStopDate) {
        stopDate = pStopDate;
    }

    public OffsetDateTime getStatusDate() {
        return statusDate;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public boolean hasCompletionChanged() {
        return completionChanged.get();
    }

    public void clearCompletionChanged() {
        completionChanged.set(false);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Integer) {
            OffsetDateTime now = OffsetDateTime.now();
            percentCompleted = (Integer) arg;
            Duration fromStart = Duration.between(startDate, now);
            estimatedCompletion = startDate.plus((fromStart.toMillis() * 100l) / percentCompleted,
                                                 ChronoUnit.MILLIS);
            completionChanged.set(true);
        }
    }
}