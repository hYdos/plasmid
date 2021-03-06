package net.gegy1000.plasmid.game.event;

public interface GameTickListener {
    EventType<GameTickListener> EVENT = EventType.create(GameTickListener.class, listeners -> {
        return () -> {
            for (GameTickListener listener : listeners) {
                listener.onTick();
            }
        };
    });

    void onTick();
}
