import 'package:flutter/material.dart';

void main() {
  runApp(TiendaTenisApp());
}

class TiendaTenisApp extends StatefulWidget {
  @override
  _TiendaTenisAppState createState() => _TiendaTenisAppState();
}

class _TiendaTenisAppState extends State<TiendaTenisApp> {
  int _selectedIndex = 0;

  // Catálogo simulado
  final List<String> productos = [
    'Nike Air Max',
    'Adidas Ultraboost',
    'Jordan 1',
    'Puma RS-X',
    'Yeezy 350'
  ];

  // Disponibilidad simulada (0.0 - 1.0)
  final Map<String, double> disponibilidad = {
    'Nike Air Max': 0.7,
    'Adidas Ultraboost': 0.6,
    'Jordan 1': 0.4,
    'Puma RS-X': 0.8,
    'Yeezy 350': 0.5,
  };

  // Carrito (simulación)
  final List<String> carrito = [];

  // Producto seleccionado para ver detalles
  String selectedProduct = 'Nike Air Max';

  final GlobalKey<ScaffoldMessengerState> messengerKey =
      GlobalKey<ScaffoldMessengerState>();

  void _onNavTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  void addToCart(String producto) {
    setState(() {
      carrito.add(producto);
    });
    messengerKey.currentState?.showSnackBar(
      SnackBar(content: Text('🛒 $producto agregado al carrito')),
    );
  }

  void selectProduct(String producto) {
    setState(() {
      selectedProduct = producto;
    });
    messengerKey.currentState?.showSnackBar(
      SnackBar(content: Text('👟 Seleccionado: $producto')),
    );
  }

  void changeAvailability(String product, double delta) {
    setState(() {
      final current = disponibilidad[product] ?? 0.0;
      var next = (current + delta).clamp(0.0, 1.0);
      disponibilidad[product] = next;
    });
  }

  @override
  Widget build(BuildContext context) {
    final screens = <Widget>[
      SearchScreen(
        productos: productos,
        onResultSelected: (p) {
          selectProduct(p);
        },
      ),
      ActionsScreen(
        productos: productos,
        onAddToCart: addToCart,
      ),
      OptionsScreen(),
      CatalogScreen(
        productos: productos,
        onSelect: (p) {
          selectProduct(p);
        },
      ),
      DetailScreen(
        product: selectedProduct,
        availability: disponibilidad[selectedProduct] ?? 0.0,
        onBuy: (p) {
          // Simula compra: reduce disponibilidad y agrega al carrito
          addToCart(p);
          changeAvailability(p, -0.15);
        },
        onRestock: (p) {
          changeAvailability(p, 0.2);
        },
      ),
    ];

    return MaterialApp(
      debugShowCheckedModeBanner: false,
      scaffoldMessengerKey: messengerKey,
      title: 'Tienda de Tenis',
      theme: ThemeData(
        primarySwatch: Colors.deepPurple,
      ),
      home: Scaffold(
        appBar: AppBar(
          title: Text('Tienda de Tenis'),
          centerTitle: true,
        ),
        body: screens[_selectedIndex],
        bottomNavigationBar: BottomNavigationBar(
          currentIndex: _selectedIndex,
          onTap: _onNavTapped,
          type: BottomNavigationBarType.fixed,
          items: [
            BottomNavigationBarItem(
                icon: Icon(Icons.search), label: 'Buscar'),
            BottomNavigationBarItem(
                icon: Icon(Icons.shopping_cart), label: 'Acciones'),
            BottomNavigationBarItem(
                icon: Icon(Icons.tune), label: 'Opciones'),
            BottomNavigationBarItem(icon: Icon(Icons.list), label: 'Catálogo'),
            BottomNavigationBarItem(
                icon: Icon(Icons.info_outline), label: 'Detalle'),
          ],
        ),
      ),
    );
  }
}

/// ---------------------- Pantalla 1: Buscar modelo ----------------------
class SearchScreen extends StatefulWidget {
  final List<String> productos;
  final void Function(String producto) onResultSelected;

  const SearchScreen({
    Key? key,
    required this.productos,
    required this.onResultSelected,
  }) : super(key: key);

  @override
  _SearchScreenState createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final TextEditingController _controller = TextEditingController();
  String _resultado = '';

  void _buscar() {
    final query = _controller.text.trim();
    if (query.isEmpty) {
      setState(() => _resultado = 'Por favor escribe un modelo de tenis.');
      return;
    }
    final encontrado = widget.productos
        .firstWhere((p) => p.toLowerCase() == query.toLowerCase(), orElse: () => '');
    if (encontrado.isNotEmpty) {
      setState(() => _resultado = '✅ El modelo "$encontrado" está disponible.');
      widget.onResultSelected(encontrado);
    } else {
      setState(() => _resultado = '❌ No se encontró "$query" en el catálogo.');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        children: [
          Text('Buscar modelo de tenis', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
          SizedBox(height: 12),
          TextField(
            controller: _controller,
            decoration: InputDecoration(
                hintText: 'Ej. Nike Air Max',
                border: OutlineInputBorder()
            ),
          ),
          SizedBox(height: 8),
          ElevatedButton(onPressed: _buscar, child: Text('Buscar')),
          SizedBox(height: 8),
          Text(_resultado),
          SizedBox(height: 16),
          Text(
            'En este fragment, el cliente puede escribir el nombre de un modelo y al presionar "Buscar" se mostrará si está disponible.',
            style: TextStyle(fontSize: 14),
          ),
        ],
      ),
    );
  }
}

/// ---------------------- Pantalla 2: Acciones rápidas ----------------------
class ActionsScreen extends StatefulWidget {
  final List<String> productos;
  final void Function(String producto) onAddToCart;

  const ActionsScreen({
    Key? key,
    required this.productos,
    required this.onAddToCart,
  }) : super(key: key);

  @override
  _ActionsScreenState createState() => _ActionsScreenState();
}

class _ActionsScreenState extends State<ActionsScreen> {
  String selected = '';

  bool showFirstImage = true;

  @override
  void initState() {
    super.initState();
    selected = widget.productos.isNotEmpty ? widget.productos[0] : '';
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        children: [
          Text('Acciones rápidas', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
          SizedBox(height: 12),
          DropdownButton<String>(
            value: selected.isNotEmpty ? selected : null,
            items: widget.productos.map((p) => DropdownMenuItem(value: p, child: Text(p))).toList(),
            onChanged: (v) => setState(() => selected = v ?? ''),
          ),
          SizedBox(height: 8),
          ElevatedButton(
            onPressed: selected.isEmpty ? null : () => widget.onAddToCart(selected),
            child: Text('Agregar al carrito'),
          ),
          SizedBox(height: 8),
          // ImageButton simulation
          IconButton(
            iconSize: 80,
            icon: Image.asset(
              'assets/images/${_fileNameFor(selected)}',
              width: 80,
              height: 80,
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => Icon(Icons.image, size: 80),
            ),
            onPressed: () {
              setState(() => showFirstImage = !showFirstImage);
              ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Vista previa: $selected')));
            },
          ),
          SizedBox(height: 12),
          Text('Este fragment permite acciones rápidas como agregar al carrito o ver la imagen del producto.'),
        ],
      ),
    );
  }
}

/// Helper: produce nombre de archivo esperado para un producto
String _fileNameFor(String product) {
  if (product.toLowerCase().contains('air max')) return 'airmax.png';
  if (product.toLowerCase().contains('ultraboost')) return 'ultraboost.png';
  if (product.toLowerCase().contains('jordan')) return 'jordan1.png';
  if (product.toLowerCase().contains('puma')) return 'pumarx.png';
  if (product.toLowerCase().contains('yeezy')) return 'yeezy350.png';
  return 'placeholder.png';
}

/// ---------------------- Pantalla 3: Opciones ----------------------
class OptionsScreen extends StatefulWidget {
  @override
  _OptionsScreenState createState() => _OptionsScreenState();
}

class _OptionsScreenState extends State<OptionsScreen> {
  bool addSocks = false;
  int paymentMethod = 0;
  bool express = false;
  String message = '';

  void showSummary() {
    setState(() {
      message =
          'Calcetas: ${addSocks ? "Sí" : "No"} • Pago: ${paymentMethod == 0 ? "Tarjeta" : "Efectivo"} • Envío: ${express ? "Exprés" : "Normal"}';
    });
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        children: [
          Text('Opciones de compra', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
          SizedBox(height: 12),
          CheckboxListTile(
            title: Text('Añadir calcetas (+\$5)'),
            value: addSocks,
            onChanged: (v) => setState(() => addSocks = v ?? false),
          ),
          RadioListTile<int>(
            title: Text('Pago con tarjeta'),
            value: 0,
            groupValue: paymentMethod,
            onChanged: (v) => setState(() => paymentMethod = v ?? 0),
          ),
          RadioListTile<int>(
            title: Text('Pago en efectivo'),
            value: 1,
            groupValue: paymentMethod,
            onChanged: (v) => setState(() => paymentMethod = v ?? 0),
          ),
          SwitchListTile(
            title: Text('Envío exprés'),
            value: express,
            onChanged: (v) => setState(() => express = v),
          ),
          ElevatedButton(onPressed: showSummary, child: Text('Mostrar selección')),
          SizedBox(height: 8),
          Text(message),
          SizedBox(height: 16),
          Text('Selecciona opciones adicionales al comprar tenis: accesorios, método de pago y envío.'),
        ],
      ),
    );
  }
}

/// ---------------------- Pantalla 4: Catálogo (ListView) ----------------------
class CatalogScreen extends StatelessWidget {
  final List<String> productos;
  final void Function(String producto) onSelect;

  const CatalogScreen({
    Key? key,
    required this.productos,
    required this.onSelect,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return ListView.separated(
      padding: EdgeInsets.all(16),
      itemCount: productos.length,
      separatorBuilder: (_, __) => Divider(),
      itemBuilder: (context, index) {
        final p = productos[index];
        return ListTile(
          leading: SizedBox(
            width: 56,
            height: 56,
            child: Image.asset(
              'assets/images/${_fileNameFor(p)}',
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => Icon(Icons.image),
            ),
          ),
          title: Text(p),
          subtitle: Text('Tap para ver detalles'),
          onTap: () => onSelect(p),
        );
      },
    );
  }
}

/// ---------------------- Pantalla 5: Detalle de producto ----------------------
class DetailScreen extends StatelessWidget {
  final String product;
  final double availability;
  final void Function(String product) onBuy;
  final void Function(String product) onRestock;

  const DetailScreen({
    Key? key,
    required this.product,
    required this.availability,
    required this.onBuy,
    required this.onRestock,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final percent = (availability * 100).round();
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        children: [
          Text('Detalles del producto', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
          SizedBox(height: 12),
          Image.asset(
            'assets/images/${_fileNameFor(product)}',
            width: 160,
            height: 160,
            errorBuilder: (_, __, ___) => Icon(Icons.image, size: 160),
          ),
          SizedBox(height: 12),
          Text(product, style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
          SizedBox(height: 8),
          LinearProgressIndicator(value: availability),
          SizedBox(height: 6),
          Text('Disponibilidad: $percent%'),
          SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(onPressed: () => onBuy(product), child: Text('Comprar (simular)')),
              SizedBox(width: 12),
              OutlinedButton(onPressed: () => onRestock(product), child: Text('Reposicionar')),
            ],
          ),
          SizedBox(height: 16),
          Text(
            'Información: imagen del producto, disponibilidad simulada y acciones para comprar o reponer stock.',
            style: TextStyle(fontSize: 14),
          ),
        ],
      ),
    );
  }
}
