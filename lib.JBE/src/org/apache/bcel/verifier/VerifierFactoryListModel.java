package org.apache.bcel.verifier;

import javax.swing.event.*;
/**
 * This class implements an adapter; it implements both a Swing ListModel and
 * a VerifierFactoryObserver.
 *
 * @version $Id: VerifierFactoryListModel.java,v 1.2 2006/09/04 15:43:18 andos Exp $
 * @author Enver Haase
 */
public class VerifierFactoryListModel implements VerifierFactoryObserver, javax.swing.ListModel{

  private java.util.ArrayList<ListDataListener> listeners = new java.util.ArrayList<ListDataListener>();

  private java.util.TreeSet<String> cache = new java.util.TreeSet<String>();

  public VerifierFactoryListModel() {
    VerifierFactory.attach(this);
    update(null); // fill cache.
  }

  public synchronized void update(String s){
    int size = listeners.size();

    Verifier[] verifiers = VerifierFactory.getVerifiers();
    int num_of_verifiers = verifiers.length;
    cache.clear();
    for (int i=0; i<num_of_verifiers; i++) {
      cache.add(verifiers[i].getClassName());
    }

    for (int i=0; i<size; i++){
      ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, num_of_verifiers-1);
      ((ListDataListener) (listeners.get(i))).contentsChanged(e);
    }
  }

  public synchronized void addListDataListener(ListDataListener l){
    listeners.add(l);
  }

  public synchronized void removeListDataListener(ListDataListener l){
    listeners.remove(l);
  }

  public synchronized int getSize(){
    return cache.size();
  }

  public synchronized Object getElementAt(int index){
    return (cache.toArray())[index];
  }
}
