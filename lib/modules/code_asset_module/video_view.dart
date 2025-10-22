import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';

class VideoView extends StatefulWidget {
  const VideoView({super.key});

  @override
  State<VideoView> createState() => _VideoViewState();
}

class _VideoViewState extends State<VideoView> {
  final VideoPlayerController videoPlayerController =
      VideoPlayerController.asset('assets/videos/music.mp4');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            ElevatedButton(
              onPressed: () async {
                await videoPlayerController.initialize();
                setState(() {});
                videoPlayerController.play();
              },
              child: Text('Play Video from Asset'),
            ),
            if (videoPlayerController.value.isInitialized)
              AspectRatio(
                aspectRatio: videoPlayerController.value.aspectRatio,
                child: VideoPlayer(videoPlayerController),
              ),
          ],
        ),
      ),
    );
  }
}
