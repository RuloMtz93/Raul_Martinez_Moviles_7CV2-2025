import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:tarea_1_elementos_de_ui_flutter/main.dart'; // <-- nombre de tu proyecto

void main() {
  testWidgets('Test de arranque de la app', (WidgetTester tester) async {
    // Ejecuta la app
    await tester.pumpWidget(TiendaTenisApp()); // <-- aquí tu clase principal

    // Busca un texto que debería aparecer al inicio
    expect(find.text('Tienda de Tenis'), findsOneWidget);
    expect(find.byType(BottomNavigationBar), findsOneWidget);
  });
}
