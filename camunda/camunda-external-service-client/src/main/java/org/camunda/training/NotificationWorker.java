package org.camunda.training;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;

public class NotificationWorker {

	public static void main(String[] args) {

		// bootstrap the client
		ExternalTaskClient client = ExternalTaskClient.create()
				.baseUrl("http://localhost:8080/engine-rest")
				.asyncResponseTimeout(20000)
				.lockDuration(10000)
				.maxTasks(1)
				.build();

		// subscribe to the topic
		TopicSubscriptionBuilder subscriptionBuilder = client.subscribe("notification");

		// handle job
		subscriptionBuilder.handler((externalTask, externalTaskService) -> {
			String content = externalTask.getVariable("tweet_content");
			System.out.println("Sorry, your tweet has been rejected: " + content);
			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("notficationTimestamp", new Date());
			variables.put("repost", false);
			externalTaskService.complete(externalTask, variables);
		});

		subscriptionBuilder.open();
	}
}