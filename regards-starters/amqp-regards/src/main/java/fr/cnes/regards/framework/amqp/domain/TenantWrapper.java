/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.framework.amqp.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import fr.cnes.regards.framework.utils.RsRuntimeException;

/**
 * Tenant wrapper.
 * This class is Cloneable because of rs-order ForwardingDataFileEventHandlerService class. See this class for more
 * informations
 * @param <T> type of event that is wrapped
 * @author svissier
 * @author oroussel
 */
public class TenantWrapper<T> implements Cloneable {

    /**
     * the event originally sent to the message broker
     */
    private T content;

    /**
     * Tenant from which the message comes from
     */
    private String tenant;

    /**
     * Event publishing date
     */
    private Date date;

    /**
     * Default constructor
     */
    public TenantWrapper() {
        // constructor for serialization
    }

    /**
     * Constructor setting the wrapper content and tenant and initializing the publishing date
     */
    public TenantWrapper(T pContent, String pTenant) {
        content = pContent;
        tenant = pTenant;
        date = new Date();
    }

    /**
     * @return the wrapper content
     */
    @JsonTypeInfo(use = Id.CLASS)
    public T getContent() {
        return content;
    }

    /**
     * Set the wrapper content
     */
    public void setContent(T pContent) {
        content = pContent;
    }

    /**
     * @return the tenant
     */
    public String getTenant() {
        return tenant;
    }

    /**
     * Set the tenant
     */
    public void setTenant(String pTenant) {
        tenant = pTenant;
    }

    /**
     * @return wrapper publishing date
     */
    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "{\"content\" : " + content + " , \"tenant\" : \"" + tenant + "\"}";
    }

    /**
     * See header for more informations
     * @return a TenantWrapper clone
     */
    @Override
    public TenantWrapper<T> clone() {
        try {
            return (TenantWrapper<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RsRuntimeException(e);
        }
    }
}
