package roboy.context.contextObjects;

import org.ros.message.MessageListener;
import roboy.context.ROSTopicUpdater;
import roboy.ros.RosMainNode;
import roboy.ros.RosSubscribers;

public class ROSTestUpdater extends ROSTopicUpdater<std_msgs.String, ROSTest> {

    @Override
    protected RosSubscribers getTargetSubscriber() {
        return RosSubscribers.TEST_TOPIC;
    }

    public ROSTestUpdater(ROSTest target, RosMainNode node) {
        super(target, node);
    }

    @Override
    protected synchronized void update() {
        target.updateValue(message.getData());
    }

}
