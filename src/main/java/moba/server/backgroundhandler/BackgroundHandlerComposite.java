package moba.server.backgroundhandler;

import java.util.ArrayList;
import java.util.List;

final public class BackgroundHandlerComposite implements BackgroundHandlerInterface {

    private final List<BackgroundHandlerInterface> children = new ArrayList<>();

    @Override
    public void halt() {
        children.forEach(BackgroundHandlerInterface::halt);
    }

    @Override
    public void start() {
        children.forEach(BackgroundHandlerInterface::start);
    }

    public void add(BackgroundHandlerInterface handler) {
        children.add(handler);
    }
}
