package de.scoopgmbh.copper.monitoring.client.form.dialog;

public interface InputDialogCreator {
    void showIntInputDialog(String labelText, int initialValue, DialogClosed<Integer> dialogClosed);

    public static interface DialogClosed<T>{
        public void closed(T inputValue);
    }
}
