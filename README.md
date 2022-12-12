# TwilioMessagingBot

Hello! This is the code I used to build a Twilio Messaging Bot I made for Gigsurf. The Controller classes I built with Java Spring Boot create REST APIs
that I use to interact with Twilio Studio and AWS DynamoDB. I host all of this code on an AWS EC2 instance to make it a publicly available website
I can access without having to keep it running on my computer. 

In addition to this, I have an external simple Spring MVC website that uses Java Spring Boot for the backend and HTML/Bootstrap as the frontend. I used Spring
Thymeleaf alongside an H2 in-memory database to be able to create a deletion/approval feature. This website is particularly for us to approve requests.
Intially, our product was going to allow users to request for services themselves, so this feature would allow us to verify their request and for
improved security. However, due to our recent pivot towards micro-internship opportunities, the team provides the requests themselves. The website is
now used as a verification system for us to see if the request is going through properly before we send it out to our 100+ users.
