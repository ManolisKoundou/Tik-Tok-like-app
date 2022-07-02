# Tik-Tok-like-app
Implementation of a Video Streaming Framework with Java programming language that delivers videos from publishers to subscribers through a broker(server), based on a certain topic or request.

The framework has been implemented with threads so multiple servers(brokers) could serve multiple users 

Users can be publishers(content creators) and subscribers

Every publisher has some channels(e.g fantasy, epic, fails etc etc), every channel has its relative videos and the videos have their own tag files with the tags that describe them 

The subscribers request from the brokers specific videos by using a certain topic that is either a channel, a tag, or both

Publishers apart from sharing videos can also create small (10-20s) videos by themselves and share them with the subscribers


