/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.staf;

import java.util.Iterator;
import java.util.List;

import fr.cnes.regards.framework.staf.domain.ArchiveAccessModeEnum;
import fr.cnes.regards.framework.staf.domain.STAFArchivingFlow;
import fr.cnes.regards.framework.staf.domain.STAFConfiguration;
import fr.cnes.regards.framework.staf.exception.STAFException;

/**
 * <p>
 * Cette classe represente une session de communication avec le STAF qui peut etre lancee en arriere plan. Cette
 * commande, necessairement non interactive ne peut etre utilisee que pour archiver des fichiers au STAF.
 * </p>
 * <p>
 * A l'issue de la session en arriere plan il est possible de verifier si elle s'est terminee de maniere nominale ou en
 * erreur.
 * </p>
 *
 * Cette classe etend AbstractSTAFBackgroundSession.
 *
 * @author CS
 */
public class STAFBackgroundSessionArchive extends AbstractSTAFBackgroundSession {

    /**
     * Liste de flots pour une session. Les fichiers sont deja repartis par flot
     */
    protected List<STAFArchivingFlow> sessionFlowList;

    protected List<String> archivedFilesList;

    /**
     * mode d'ecrasement ou pas lors de l'archivage.
     */
    protected boolean replace;

    /**
     * Constructeur
     *
     * @param pSessionId
     *            l'identifiatn de session.
     * @param pProject
     *            Nom du projet STAF contenant les fichiers a archiver
     * @param pPassword
     *            Mot de passe STAF permettant de se connecter
     * @param pGFAccount
     *            Indique si le projet STAF est un GF (Gros Fichiers)
     * @param pFilesFlow
     *            Liste de fichiers deja reparti dans les flots
     * @param pDirectory
     *            Repertoire du STAF dans lequel les fichiers seront archives
     */
    public STAFBackgroundSessionArchive(Integer pSessionId, String pProject, String pPassword,
            List<STAFArchivingFlow> pFilesFlow, String pDirectory, List<String> pArchivedFilesList, boolean pReplace,
            STAFConfiguration pConfiguration) {
        // Call super
        super(pSessionId, pProject, pPassword, pDirectory, pConfiguration);
        // Files list already dispatch by flow
        sessionFlowList = pFilesFlow;
        archivedFilesList = pArchivedFilesList;
        configuration = pConfiguration;
        replace = pReplace;

    }

    /**
     * Methode surchargee
     *
     * Archive les fichiers de maniere bufferisee (par flots) L ensemble de fichiers sont deja dispaches par flow :
     * sessionFlowList_ On boucle sur sessionFlowList_ pour lancer l archivage bufferise
     */
    @Override
    public void doProcess() throws STAFException {
        final Iterator<STAFArchivingFlow> iter = sessionFlowList.iterator();
        while (iter.hasNext()) {
            final STAFArchivingFlow flow = iter.next();
            // Archive files by bufferisation (by flow)
            archivedFilesList.addAll(session.staffilArchive(flow.getFilesMap(), flow.getServiceClass(), replace));
        }
    }

    /**
     * Methode surchargee
     *
     * Liberation de la ressource occupee par la session
     */
    @Override
    public void freeReservation() throws STAFException {
        // Frees ressource used by the session
        STAFSessionManager.getInstance(configuration).freeReservation(sessionId, ArchiveAccessModeEnum.ARCHIVE_MODE);
    }

    public List<String> getArchivedFilesList() {
        return archivedFilesList;
    }
}