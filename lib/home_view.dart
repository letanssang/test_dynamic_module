import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

// import deferred module
import 'images_page_view.dart';
import 'modules/code_asset_module/code_asset_module.dart'
    deferred as code_asset_module;

class HomeView extends StatefulWidget {
  const HomeView({super.key});

  @override
  State<HomeView> createState() => _HomeViewState();
}

class _HomeViewState extends State<HomeView> {
  bool _isLoading = false; // trạng thái đang tải module
  String _loadingMessage = '';

  Future<void> _loadCodeAssetModule() async {
    try {
      setState(() {
        _isLoading = true;
        _loadingMessage = 'Đang tải module Code + Asset...';
      });

      // Gọi loadLibrary()
      await code_asset_module.loadLibrary();

      setState(() {
        _isLoading = false;
        _loadingMessage = '';
      });

      // Navigate sang VideoView trong module đã load
      if (mounted) {
        Navigator.of(context).push(
          MaterialPageRoute(builder: (_) => code_asset_module.VideoView()),
        );
      }
    } catch (e, s) {
      if (kDebugMode) {
        print('Lỗi khi tải module: $e\n$s');
      }
      setState(() {
        _isLoading = false;
        _loadingMessage = 'Tải module thất bại!';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        Scaffold(
          appBar: AppBar(title: const Text('Demo Deferred Component Flutter')),
          body: Column(
            children: [
              Expanded(
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    _buildModuleItem(
                      'Module Code + Asset',
                      _loadCodeAssetModule,
                    ),
                    _buildModuleItem('Module Asset', () {
                      Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) => const ImagesPageView(),
                        ),
                      );
                    }),
                  ],
                ),
              ),
            ],
          ),
        ),

        // Overlay loading
        if (_isLoading)
          Container(
            color: Colors.black54,
            child: Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const CircularProgressIndicator(color: Colors.white),
                  const SizedBox(height: 16),
                  Text(
                    _loadingMessage,
                    style: const TextStyle(color: Colors.white, fontSize: 16),
                  ),
                ],
              ),
            ),
          ),
      ],
    );
  }

  Widget _buildModuleItem(String title, VoidCallback onPressed) {
    return GestureDetector(
      onTap: onPressed,
      child: Container(
        margin: const EdgeInsets.all(8),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.blueAccent,
          borderRadius: BorderRadius.circular(8),
        ),
        child: Text(
          title,
          style: const TextStyle(color: Colors.white, fontSize: 18),
        ),
      ),
    );
  }
}
