package es.gob.afirma.miniapplet.actions;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.miniapplet.Messages;

/**
 * Acci&oacute;n para almacenar un fichero en disco.
 * @author Carlos Gamuci Mill&aacute;n
 */
public class SaveFileAction implements PrivilegedExceptionAction<Boolean> {
    
	private String title;
	
    private byte[] data;
    
    private String[] exts;
    
    private String desc;
    
    private File fileHint;
    
    private Component parent;
        
    /**
     * Crea la acci&oacute;n para la carga de ficheros.
     * @param title T&iacute;tulo del di&aacute;logo de guardado.
     * @param data Datos que se desean guardar.
     * @param exts Extensiones permitidas para los datos.
     * @param description Descripci&oacute;n del tipo de fichero.
     * @param fileHint Fichero de salida propuesto.
     * @param parent Componente padre sobre el que se mostrar&aacute; el di&aacute;logo.
     */
    public SaveFileAction(final String title, final byte[] data, final String[] exts,
    		final String description, final File fileHint, final Component parent) {
        this.title = title;
        this.data = data;
        this.exts = exts;
        this.desc = description;
        this.fileHint = fileHint;
        this.parent = parent;
    }
    
    /**
     * Muestra un di&aacute;logo modal para el guardado de un fichero y lo salva en el directorio
     * y con el nombre indicado por el usuario.
     * @return {@code true} si el fichero se almacen&oacute; correctamente.
     * @throws AOCancelledOperationException Cuando se cancela la operaci&oacute;n.
     * @throws IOException Cuando se produce un error al almacenar el fichero.
     */
	@Override
	public Boolean run() throws AOCancelledOperationException, IOException {
		
		File file = this.selectFileToSave();
		
    	return Boolean.valueOf(this.saveFile(file, this.data));
	}
	
    /**
     * Crea un filtro de fichero por extensi&oacute;n.
     * @param extensions Extensiones de fichero permitidas.
     * @param description Descripci&oacute;n del tipo de fichero.
     * @return
     */
    private FileFilter getExtensionFileFilter(final String[] extensions, final String description) {
    	return new FileFilter() {
			@Override
			public String getDescription() {
				return description;
			}
			
			@Override
			public boolean accept(File f) {
				for (String ext : extensions) {
					if (f.getName().endsWith(ext)) {
						return true;
					}
				}
				return false;
			}
		};
    }
    
    /**
     * Pregunta al usuario por un nombre de fichero para salvar datos en disco.
     * @return Nombre de fichero (con ruta) seleccionado por el usuario
     * @throws IOException Cuando se produzca un error durante la selecci&oacute;n del fichero.
     * @throws AOCancelledOperationException Cuando el usuario cancele la operaci&oacute;n.
     */
    private final File selectFileToSave() throws IOException, AOCancelledOperationException {

    	final JFileChooser fc = new JFileChooser();
    	if (this.title != null) {
    		fc.setDialogTitle(this.title);
    	}
    	if (this.exts != null) {
    		fc.setFileFilter(this.getExtensionFileFilter(this.exts, this.desc));
    	}
    	if (this.fileHint != null) {
    		fc.setSelectedFile(this.fileHint);
    	}
		
    	boolean selectedFile = false;
        File finalFile = null;
        do {
            final int ret = fc.showSaveDialog(this.parent);
            if (ret == JFileChooser.CANCEL_OPTION) {
            	throw new AOCancelledOperationException();
            }
            if (ret == JFileChooser.ERROR_OPTION) {
            	throw new IOException();
            }
            final File tempFile = fc.getSelectedFile();
            if (tempFile.exists()) {
            	if (tempFile.isDirectory() || !tempFile.canWrite()) {
            		JOptionPane.showMessageDialog(this.parent,
            				Messages.getString("SaveFileAction.0", tempFile.getAbsolutePath()), //$NON-NLS-1$
            				Messages.getString("SaveFileAction.1"), //$NON-NLS-1$
            				JOptionPane.WARNING_MESSAGE);
            		continue;
            	}
            	final int resp =
            		JOptionPane.showConfirmDialog(this.parent,
            				Messages.getString("SaveFileAction.2", tempFile.getAbsolutePath()), //$NON-NLS-1$
            				Messages.getString("SaveFileAction.1"), //$NON-NLS-1$
            				JOptionPane.YES_NO_CANCEL_OPTION,
            				JOptionPane.QUESTION_MESSAGE);
            	if (resp == JOptionPane.YES_OPTION) { // Sobreescribir fichero
            		finalFile = tempFile;
            		selectedFile = true;
            	}
            	else if (resp == JOptionPane.NO_OPTION) { // Seleccionar fichero
            		continue;
            	}
            	else { // Cancelar operacion de guardado
            		throw new AOCancelledOperationException();
            	}
            }
            else {
            	finalFile = fc.getSelectedFile();
            	selectedFile = true;
            }
        } while (!selectedFile);

        return finalFile;
    }
    
    private boolean saveFile(final File file, final byte[] dataToSave) throws IOException {
    	
    	FileOutputStream fos = new FileOutputStream(file);
    	fos.write(dataToSave);
    	try {
    		fos.close();
    	} catch (Exception e) {
    		/* No hacemos nada */
		}
    	
    	return true;
    }
}
