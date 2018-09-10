package org.roboy.context.contextObjects;

import org.roboy.context.InternalUpdater;

/**
 * Update the history of intents
 */
public class DialogIntentsUpdater extends InternalUpdater<DialogIntents, IntentValue> {
    public DialogIntentsUpdater(DialogIntents target) {
        super(target);
    }
}