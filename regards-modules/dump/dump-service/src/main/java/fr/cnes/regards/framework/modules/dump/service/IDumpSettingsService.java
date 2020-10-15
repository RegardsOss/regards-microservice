/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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


package fr.cnes.regards.framework.modules.dump.service;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.dump.domain.DumpSettings;

/**
 * Service to handle {@link DumpSettings}
 * //FIXME: Dump Manager Service used to update the {@link FeatureSaveMetadataScheduler} from {@link FeatureConfigurationManager}
 * @author Iliana Ghazali
 */
public interface IDumpSettingsService {
    /**
     * Retrieve {@link DumpSettings} from database. If they do not exist, new ones are created by default and saved in database.
     * @return DumpSettings
     */
    DumpSettings retrieve();

    /**
     * Update {@link DumpSettings} with the new settings provided. If DumpSettings are not already in the database,
     * {@link EntityNotFoundException} is thrown.
     * @param dumpSettings new DumpSettings
     * @return DumpSettings updated
     * @throws EntityNotFoundException
     */
    DumpSettings update(DumpSettings dumpSettings) throws EntityNotFoundException;

    /**
     * Reset the last dump request date by putting a null value
     * FIXME: The next dump will then contain all content present in the database
     */
    void resetLastDumpDate();
}
