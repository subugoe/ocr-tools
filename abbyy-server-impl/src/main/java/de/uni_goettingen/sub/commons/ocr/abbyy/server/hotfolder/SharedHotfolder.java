package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;


//TODO: Finish this, it will be needed if we are using concurrent processes on one Hotfolder

/**
 * The Interface SharedHotfolder. Represents a Hotfolder that might be shared
 * between different Instances. To be able to ensure an exclussive access there
 * are several methods to lock, unlock and simple communication about the state
 * of the Hotfolder.
 */
public interface SharedHotfolder extends Hotfolder {
	abstract public void setHotfolder (Hotfolder hotfolder);

	/**
	 * Adds a lock to the Hotfolder, this might be a simple ".lock" file or a
	 * more elaborate files system based lock. Implementers shouldn't make any
	 * assumtions of the type of the lock since it might be protected by a
	 * session key or shared secret.
	 */
	abstract public void lock ();

	/**
	 * Unlocks a Hotfolder, this will fail, if the current instance ism't
	 * holding this lock. It is possible to try to force the removal of this
	 * lock, But it's not guaranteed that this operation succeeds. Use the
	 * "force" parameter with care as this might break other applications that
	 * are using the Hotfolder concurrently. Note that the forced operation
	 * might fail.
	 * 
	 * @param force
	 *            true if a forced removal should be attempted
	 * @return true if the lock was successfully removed, false otherwise
	 */
	abstract public Boolean unlock (Boolean force);

	abstract public void setState (HotfolderState state);

	abstract public HotfolderState getState ();

}
