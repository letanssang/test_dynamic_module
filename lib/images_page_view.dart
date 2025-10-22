import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ImagesPageView extends StatefulWidget {
  const ImagesPageView({super.key});

  @override
  State<ImagesPageView> createState() => _ImagesPageViewState();
}

class _ImagesPageViewState extends State<ImagesPageView> {
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    Future.microtask(() async {
      try {
        await DeferredComponent.installDeferredComponent(
          componentName: 'asset_only_module',
        ).then(
          (value) => setState(() {
            isLoading = false;
          }),
        );
      } catch (e) {
        setState(() {
          isLoading = false;
        });
        print('Error installing deferred component: $e');
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    //return pageview images assets/images/image1.png to assets/images/image9.png
    return Scaffold(
      appBar: AppBar(title: const Text('Images Page View')),
      body: PageView.builder(
        itemCount: 9,
        itemBuilder: (context, index) {
          final imageIndex = index + 1;
          return Center(
            child: Image.asset(
              width: 300,
              fit: BoxFit.contain,
              'assets/images/image$imageIndex.jpg',
            ),
          );
        },
      ),
    );
  }
}
