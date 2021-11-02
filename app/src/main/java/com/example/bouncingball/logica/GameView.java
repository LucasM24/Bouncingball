package com.example.bouncingball.logica;

import static androidx.core.content.ContextCompat.startActivity;

import static android.graphics.Color.*;

import static com.example.bouncingball.R.font.fuente_uno;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.fonts.FontFamily;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import com.example.bouncingball.R;
import com.example.bouncingball.activity.SiguienteNivel;
import com.example.bouncingball.activity.Perdedor;
import com.example.bouncingball.activity.Ganador;
import com.example.bouncingball.clases.Bloque;
import com.example.bouncingball.clases.Jugador;
import com.example.bouncingball.clases.Pelota;
import com.example.bouncingball.database.dbConexion;
import com.example.bouncingball.hilos.GameThread;

import java.util.ArrayList;


public class GameView extends SurfaceView {
	//Base de datos
	private dbConexion dao ;

	//Pinceles
	private Paint pincelIndicadores = new Paint();
	private Paint pincelPelota = new Paint();
	private Paint pincelJugador = new Paint();
	private Paint pincelDureza =  new Paint();

	//Indicadores
	private int vidas;
	private int puntaje;
	private int nivel;
	private boolean cayo;
	private int velocidadPelota;
	//Elementos
	private Grilla grilla;
	private Pelota pelota;
	//	private Bloque jugador;
	private Jugador jugador;
	private GameThread gameThread;
	private int xMax, yMax;
	//Dedo
	private int posDedoX;
	private int posDedoY;
	//Inicio del juego
	private boolean inicioJuego = false;

	private boolean pantallaPulsada = false;

	//imagen Game over
	private boolean imgFinJuego=false;

	private boolean ganoNivel=false;
	private boolean jugando=true;

	//imagen Gano
	private boolean imgSuperoNivel=false;
	private Bitmap bmp;
	private Bitmap pelotaImg;
	private Bitmap jugadorImg;
	private Bitmap fondoImg;

    private int puntajeInicial ;

	private Bitmap panelSuperior;


	private boolean juegoEnPausa = false;
	private boolean siguienteFotograma = false;

	//Sonido
	private MediaPlayer mp ;



	public GameView(Context context) {
		super(context);
		SurfaceHolder holder = getHolder();
		holder.addCallback(new SurfaceHolder.Callback() {
			@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
			@Override
			public void surfaceCreated(SurfaceHolder surfaceHolder) {
				setWillNotDraw(false);
				nivel = 2;
				vidas = 1;
				puntaje = 0;
				cayo = false;
				xMax = getWidth();
				yMax = getHeight();
				velocidadPelota=7;



				int numeroAleatorio = (int) (Math.random() * 2);
				if(numeroAleatorio==1){
					velocidadPelota=-7;
				}



				//Datos archivos
				SharedPreferences preferences = getContext().getSharedPreferences("myidiom", Context.MODE_PRIVATE);
				String user = preferences.getString("user","vacio");
				System.out.println("Usuario Recibido :"+user);
				// Base de Datos
				dao = new dbConexion(getContext());

				//Recuperar el clave-valor de Archivo
				puntaje = preferences.getInt("user_puntaje",0);
				puntajeInicial = puntaje;
				// Si seteo al puntaje total
				SharedPreferences.Editor editor = preferences.edit();
				//editor.putInt("puntaje_total",0);
				//editor.commit();*/

				//Ubicacion del jugador
//				 jugador= new Jugador(150,227,150,20);
				// pelota = new Pelota(jugador.getPosX(),jugador.getPosY()-15,15, 15);

				/*
				 * Aqui deberia recuperarse del sharedPreferences
				 * */
				editor = preferences.edit();
				int level = preferences.getInt("level",1);
				//int level = preferences.getInt("level",1);
				editor.putInt("level", level);
				editor.commit();
				System.out.println("Nivel actual " + level);

				grilla = new Grilla(xMax, yMax, 7, 10, 40,level ,context);

				jugador = new Jugador((getWidth() / 2) - (150 / 2), getHeight() - 200, 150,20);
				//jugador = new Jugador((getWidth() / 2) - (150 / 2), 200, 150,20);
				pelota = new Pelota(jugador.getPosX(),jugador.getPosY()-20,23, velocidadPelota);
				//grilla = new Grilla(xMax, yMax, 7, 10, 40,context);

				pelotaImg = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pelota), pelota.getTamanio(), pelota.getTamanio(),false);
				jugadorImg= Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.jugador), jugador.getAncho(), jugador.getAlto(),false);
				fondoImg = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fondo1),xMax,yMax,false);
				panelSuperior = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.panel),xMax,120,false);

				//Pinceles
				pincelPelota.setColor(BLUE);
				pincelJugador.setColor(GREEN);
				pincelIndicadores.setColor(WHITE);
				pincelIndicadores.setTextSize(30f);
				Typeface typeface = ResourcesCompat.getFont(context, fuente_uno);
				pincelIndicadores.setTypeface(typeface);
				pincelDureza.setColor(RED);

				gameThread = new GameThread(GameView.this);
				gameThread.play();

			}

			@Override
			public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

			}

			@Override
			public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

			}
		});
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//Propiedades del canvas
		super.onDraw(canvas);

		if (jugando) {


		canvas.drawBitmap(fondoImg, 0, 120, null);
		canvas.drawBitmap(panelSuperior, 0, 0, null);


		//System.out.println("Entro al onDraw***********************************************************");


		//Controles
		verificarContactoPantalla();

		//pelota.actualizarPosicion();
		if (vidas >= 0) {

			//Es el ultimo bloque??
			boolean gano = this.grilla.getCantidadBloquesPintados() == 0;
			if(this.grilla.getCantidadBloquesPintados()==10){
				gameThread.aumentarVelocidad();
			}
			if (gano || ganoNivel) {
				this.nivelSuperado(canvas);
				System.out.println("Despues del nivel superado");
			} else {
				//actualizamos las posiciones
				if (!cayo) {
					//Verifico contacto si la posicion es menor que

					this.verificarContactosMultiples(canvas);
					this.pintarGrilla(canvas);


					//Aca va el metodo pintar grilla
					this.controlDelJuego(canvas);
//					testPausarJuego();

				} else {
					//La pelota pasa al jugador
					this.restarVida();
				}
				//Pinta los indicadores de vida y puntaje
				this.dibujarIndicadores(canvas);
				canvas.drawBitmap(jugadorImg, jugador.getPosX(), jugador.getPosY(), null);
			}
		} else {
			//Si pierdo todas las vidas
			this.reiniciarJuego(canvas);
		}
		//Posición del boton siguiente
		/*
		canvas.drawRect(xMax-100, yMax-100, xMax+50, yMax+50, pincelPelota);
		canvas.drawRect(0, yMax-100, 50, yMax-50, pincelPelota);
		if(siguienteFotograma){
			gameThread.pause();
			siguienteFotograma=false;
		}*/

	}
	}

	//Metodos del Juego
	private ArrayList<Bloque> obtenerBloquesChocados(){
		boolean huboContacto = false;
		int contarContactos = 0;

		ArrayList<Bloque> listaBloque = new ArrayList<Bloque>();
		//Recorre todas las filas verificando si la pelota tuvo algun contacto
		for (int i = 0; i < this.grilla.getCantidadFilas(); i++){
			for (int j = 0; j < this.grilla.getCantidadColumnas(); j++){
				Bloque bloque = this.grilla.getBloque(i, j);
				//Verifica que el bloque todavia no se rompio
				if(bloque.getDureza() == 1 || bloque.getDureza() == 2){
					//optimizar: Para que si ya detecto un contacto, no siga verificando los otros bloques
//					unContacto = verificarContacto(bloque);
                    huboContacto = pelota.interseccion(bloque.getModelo());
					if(huboContacto){
						// Para hacer pruebas
						//Guardo el bloque que hizo contacto
						listaBloque.add(bloque);
						if(bloque.getDureza() == 1){
							this.puntaje+= bloque.getPuntaje();
						}else{
							//Agregar la imagen del bloque quebrado!!
							//bloque.setPincel(pincelDureza);
							bloque.setImagenBloqueRoto();
						}
						contarContactos++;
					}
				}
			}
		}
		return listaBloque;
	}

	private void verificarContactosMultiples(Canvas canvas){
		Bloque [] arreBloqueContacto;
		ArrayList<Bloque> listaBloque;
		listaBloque=this.obtenerBloquesChocados();

		if(listaBloque.size() == 1){
			System.out.println("Contacto 1");
			Bloque b=listaBloque.get(0);
			contactoUnBloque(b);
		}else if(listaBloque.size() == 2){
			System.out.println("Contacto 2");
			Bloque b1=listaBloque.get(0);
			Bloque b2=listaBloque.get(1);
			contactoDosBloques(b1,b2);
		} else if(listaBloque.size() == 3){
			System.out.println("Contacto 3");
			Bloque b1=listaBloque.get(0);
			Bloque b2=listaBloque.get(1);
			Bloque b3=listaBloque.get(2);
			contactoTresBloques(b1,b2,b3);
		}

	}

	private void contactoUnBloque(Bloque bloque){
		int areaDeContacto = bloque.getAreaDeContacto(this.pelota);
//		actualizarDireccion2(b);
        this.pelota.setDireccion(areaDeContacto, "Bloque");

		if(bloque.getDureza() == 1) {
			bloque.setDureza(0);
			this.grilla.restarBloquesPintados();
		}else{
		    //Se usa cuando el bloque tiene dureza 2 (nivel dificil)
			bloque.setDureza(1);
		}

	}

	private void contactoDosBloques(Bloque b1, Bloque b2){
		//Estan horizontal
		if(b1.getNroFila()==b2.getNroFila()){
			//Tiene que desaparecer los dos y la dirección solo cambia eje Y
			if(b1.getDureza()==1 && b2.getDureza()==1) {
				b1.setDureza(0);
				b2.setDureza(0);
				this.grilla.restarBloquesPintados();
				this.grilla.restarBloquesPintados();
			}else{
				if(b1.getDureza()==2 && b2.getDureza()==2) {
					b1.setDureza(1);
					b2.setDureza(1);
				}else{
					if(b1.getDureza() == 1){
						b1.setDureza(0);
						b2.setDureza(1);
					}else{
						b2.setDureza(0);
						b1.setDureza(1);
					}
					this.grilla.restarBloquesPintados();
				}
			}
			pelota.setDireccionEnY(-1 * pelota.getDireccionEnY());
		}
		//Estan vertical
		if(b1.getNroColumna() == b2.getNroColumna()){
			System.out.println("Bloques estan Vertical");
			//Tiene que desaparecer los dos y la dirección solo cambia eje Y
			b1.setDureza(0);
			b2.setDureza(0);
			this.grilla.restarBloquesPintados();
			this.grilla.restarBloquesPintados();
			pelota.setDireccionEnX(-1 * pelota.getDireccionEnX());
		}
		//Estan en diagonal
		if(b1.getNroColumna() != b2.getNroColumna() && b1.getNroFila() != b2.getNroFila()){
			System.out.println("Bloques estan diagonal");
			//Tiene que desaparecer los dos y la dirección solo cambia eje Y
			b1.setDureza(0);
			b2.setDureza(0);
			this.grilla.restarBloquesPintados();
			this.grilla.restarBloquesPintados();
			pelota.setDireccionEnX(-1 * pelota.getDireccionEnX());
			pelota.setDireccionEnY(-1 * pelota.getDireccionEnY());
		}

	}

	private void contactoTresBloques(Bloque b1, Bloque b2, Bloque b3){
		System.out.println("Contacto Tres Bloques ");
		System.out.println("Bloque 1 Columna: " + b1.getNroColumna() + " Fila: " + b1.getNroFila());
		System.out.println("Bloque 2 Columna: " + b2.getNroColumna() + " Fila: " + b2.getNroFila());
		System.out.println("Bloque 3 Columna: " + b3.getNroColumna() + " Fila: " + b3.getNroFila());

		//Caso 1
		if(b2.getNroColumna() != b3.getNroColumna() && b2.getNroFila() != b3.getNroFila()){
			//Tiene que desaparecer los dos y la dirección solo cambia eje Y
			System.out.println("Caso 1 ");
			b2.setDureza(0);
			b3.setDureza(0);
			this.grilla.restarBloquesPintados();
			this.grilla.restarBloquesPintados();
			pelota.setDireccionEnX(-1 * pelota.getDireccionEnX());
			pelota.setDireccionEnY(-1 * pelota.getDireccionEnY());
		}
		//Caso 2 y caso 4
		if(b1.getNroColumna()!=b3.getNroColumna()&&b1.getNroFila()!=b3.getNroFila()){
			//Tiene que desaparecer los dos y la dirección solo cambia eje Y
			System.out.println("Caso 2 y 4 ");
			b1.setDureza(0);
			b3.setDureza(0);
			this.grilla.restarBloquesPintados();
			this.grilla.restarBloquesPintados();
			pelota.setDireccionEnX(-1 * pelota.getDireccionEnX());
			pelota.setDireccionEnY(-1 * pelota.getDireccionEnY());
		}
		//Caso 3
		if(b1.getNroColumna() != b2.getNroColumna() && b1.getNroFila() != b2.getNroFila()){
			//Tiene que desaparecer los dos y la dirección solo cambia eje Y
			System.out.println("Caso 3 ");
			b1.setDureza(0);
			b2.setDureza(0);
			this.grilla.restarBloquesPintados();
			this.grilla.restarBloquesPintados();
			pelota.setDireccionEnX(-1 * pelota.getDireccionEnX());
			pelota.setDireccionEnY(-1 * pelota.getDireccionEnY());
		}

	}

	private void pintarGrilla(Canvas canvas){
		for (int i = 0; i < this.grilla.getCantidadFilas(); i++){
			for (int j = 0; j < this.grilla.getCantidadColumnas(); j++){
				Bloque bloque = this.grilla.getBloque(i, j);
				//Este metodo dibuja la grilla creada anteriormente.
				if(bloque.getDureza() == 1){
					canvas.drawBitmap(bloque.getImagen(), bloque.getPosX(), bloque.getPosY(), null);
//					canvas.drawRect(bloque.getModelo(), bloque.getPincel());
				}
				if(bloque.getDureza() == 2){
					canvas.drawBitmap(bloque.getImagen(), bloque.getPosX(), bloque.getPosY(), null);
//					canvas.drawRect(bloque.getModelo(), bloque.getPincel());
				}
			}
		}
	}

	private void nivelSuperado(Canvas canvas){

		/*
		 *
		 * case one verify what the limit maximum for plays the can  do the playe
		 *
		 * */
		SharedPreferences preferences = getContext().getSharedPreferences("myidiom", Context.MODE_PRIVATE);
		String user = preferences.getString("user","vacio");
		jugando=false;
		ganoNivel=true;
		if(this.grilla.getNivelActual()<3) {
			imgSuperoNivel = true;
			gameThread.parar();
			this.reubicarPelota();

			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt("user_puntaje", this.puntaje);
			System.out.println("Puntaje  : "+this.puntaje+"en el nivel : "+this.grilla.getNivelActual());
			editor.commit();

			this.grilla.avanzarUnNivel();

			int nivelActual = this.grilla.getNivelActual();
			editor.putInt("level", nivelActual);
			editor.commit();

			Intent i = new Intent(getContext(), SiguienteNivel.class);
			i.putExtra("id_user",user);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(getContext(), i, null);


			System.out.println("********************Termino el hilo");

		}else{
			gameThread.parar();
			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt("user_puntaje", this.puntaje);
			System.out.println("Puntaje  : "+this.puntaje+"en el nivel : "+this.grilla.getNivelActual());
			editor.commit();

			Intent h = new Intent(getContext(), Ganador.class);
			h.putExtra("id_user",user);
			h.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(getContext(), h, null);
			Toast.makeText(this.getContext(), "!!!Ganaste!!!", Toast.LENGTH_SHORT).show();
		}
	}


	private void controlDelJuego(Canvas canvas){
		if (inicioJuego && (pelota.getY() < jugador.getPosY()) && (pelota.getY() > (jugador.getPosY() - 50))) {
			verificarContactoJugador2();
		}

		//actualizamos las posiciones
		if (pantallaPulsada) {
			//Actualizar posicion del jugador
			transicionEnX();
			jugador.inicializarAreas();
		}
		if (inicioJuego) {
			//Pelota en movimiento
			pelota.actualizarPosicion();
			canvas.drawBitmap(pelotaImg, pelota.getX(), pelota.getY(), null);
		} else {
			//La pelota esta en la posicion del jugador
			pelota.setX(jugador.getPosX() + (jugador.getAncho() / 2) - pelota.getTamanio() / 2);
			canvas.drawBitmap(pelotaImg, pelota.getX(), pelota.getY(), null);
		}
	}

	private void restarVida(){
		vidas += -1;
		//Reubicar la pelota cuando se cae
		this.reubicarPelota();

		cayo = false;
		inicioJuego=false;
	}

	private void dibujarIndicadores(Canvas canvas){
		SharedPreferences preferences = getContext().getSharedPreferences("myidiom", Context.MODE_PRIVATE);

		String idioma_user = preferences.getString("idioma","es");
		float centroNivel=((xMax * 10) / 100)/2;
		float centroPuntaje=((xMax * 40) / 100)/2;
		float centroVida=((xMax * 75) / 100)/2;

		if(idioma_user.equalsIgnoreCase("es")){
			//Nivel
			canvas.drawText("Nivel " , (xMax * 10) / 100, 50f, pincelIndicadores);
			canvas.drawText(String.valueOf(this.grilla.getNivelActual()), ((xMax * 10) / 100)+centroNivel, 90f, pincelIndicadores);
			//Puntaje
			canvas.drawText("Puntaje " , (xMax * 40) / 100, 50f, pincelIndicadores);
			canvas.drawText( String.valueOf(this.puntaje), ((xMax * 40) / 100)+45, 90f, pincelIndicadores);
			//Vidas
			if(vidas>0){

				canvas.drawText("Vidas " , (xMax * 75) / 100, 50f, pincelIndicadores);
				canvas.drawText(String.valueOf(this.vidas), ((xMax * 75) / 100)+45, 90f, pincelIndicadores);
			}else{

				canvas.drawText("Vidas " , (xMax * 75) / 100, 50f, pincelIndicadores);
				canvas.drawText(String.valueOf(0), ((xMax * 75) / 100)+45, 90f, pincelIndicadores);
			}

		}
		else{

			//Nivel
			canvas.drawText("Level " , (xMax * 10) / 100, 50f, pincelIndicadores);
			canvas.drawText(String.valueOf(this.grilla.getNivelActual()), ((xMax * 10) / 100)+centroNivel, 90f, pincelIndicadores);
			//Puntaje
			canvas.drawText("Score " , (xMax * 40) / 100, 50f, pincelIndicadores);
			canvas.drawText( String.valueOf(this.puntaje), ((xMax * 40) / 100)+45, 90f, pincelIndicadores);
			//Vidas
			if(vidas>0){

				canvas.drawText("Life " , (xMax * 75) / 100, 50f, pincelIndicadores);
				canvas.drawText(String.valueOf(this.vidas), ((xMax * 75) / 100)+45, 90f, pincelIndicadores);
			}else{

				canvas.drawText("Life " , (xMax * 75) / 100, 50f, pincelIndicadores);
				canvas.drawText(String.valueOf(0), ((xMax * 75) / 100)+45, 90f, pincelIndicadores);
			}

		}
	}

	private void reiniciarJuego(Canvas canvas){
		SharedPreferences preferences = getContext().getSharedPreferences("myidiom", Context.MODE_PRIVATE);
		String user = preferences.getString("user","vacio");
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("user_puntaje",puntajeInicial);
		editor.commit();

		vidas = 2;
		puntaje=0;
		imgFinJuego=true;
		this.grilla.setCantidadBloquesPintados(0);
		this.grilla.reiniciarGrilla();
		gameThread.pause();
		Intent j = new Intent(getContext(), Perdedor.class);
		j.putExtra("id_user",user);
		j.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(getContext(), j, null);
		Toast.makeText(this.getContext(), "!!!Perdiste el Juego !!!", Toast.LENGTH_SHORT).show();
		//bmp= Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.finjuego),xMax,yMax,false);
		//canvas.drawBitmap(bmp, 0, 0, null);

	}

	private void reubicarPelota(){
		pelota.setX(jugador.getPosX() + (jugador.getAncho() / 2));
		pelota.setY(jugador.getPosY()- pelota.getTamanio()-2);
		int numeroAleatorio = (int) (Math.random() * 2);
		if(numeroAleatorio==1){
			pelota.setDireccionEnX(-pelota.getVelocidad());
		}else{
			pelota.setDireccionEnX(pelota.getVelocidad());
		}

		pelota.setDireccionEnY(-pelota.getVelocidad());
	}

	public boolean onTouchEvent(MotionEvent evento) {
		//if (evento.getAction() == MotionEvent.ACTION_DOWN) {}
		//Pulso la pantalla
		pantallaPulsada = true;
		posDedoX = (int) evento.getX();
		posDedoY = (int) evento.getY();

		//invalidate();

		//    if(evento.getAction() == MotionEvent.ACTION_UP){}
		// MotionEvent.ACTION_DOWN
		if(imgFinJuego || imgSuperoNivel){
			if (evento.getAction() == MotionEvent.ACTION_UP) {
				//Si perdio o gano
				if(posDedoX>50&&posDedoX<(xMax-50)){
					if(posDedoY>600&&posDedoY<(yMax-300)){
						gameThread.continuar();
						imgFinJuego=false;
						imgSuperoNivel=false;
					}
				}
			}
		}else{
			if(evento.getAction() == MotionEvent.ACTION_UP){
				//System.out.println("Pulso la pantalla *********************");
				inicioJuego = true;
				pantallaPulsada = false;
			}
		}


		//Boton continuar(Borrar cuando se saque la imagen)
		if(evento.getAction() == MotionEvent.ACTION_UP){
			if(posDedoX>50&&posDedoX<(xMax-50)){
				if(posDedoY>10&&posDedoY<300){
					gameThread.continuar();
					System.out.println("Continuar jugando Boton Segui jugando #0# ");

				}
			}
		}
		if(juegoEnPausa && !siguienteFotograma){
			if (evento.getAction() == MotionEvent.ACTION_UP) {
				if(posDedoX>xMax-100){
					if(posDedoY>yMax-100){
						gameThread.continuar();
						System.out.println("Continuar jugando Boton Segui jugando [1] ");
						siguienteFotograma=true;
					}
				}
			}

		}

		if(juegoEnPausa && !siguienteFotograma){
			if (evento.getAction() == MotionEvent.ACTION_UP) {
				if(posDedoX<100){
					if(posDedoY>yMax-100){
						gameThread.continuar();
						System.out.println("Continuar jugando Boton Segui jugando *2* ");

					}
				}
			}

		}
		return true;
	}

	//Controlar rebote
	public void transicionEnX() {
		int posNueva = posDedoX - (jugador.getAncho() / 2);
		//int posNueva=posDedoX;
		int velocidadTransicion = 50;
		int rangoMovimiento = 50;
		int posJugadorCentro = jugador.getPosX() + (jugador.getAncho() / 2);
		//Verifica que el jugador no salga del limite izquierdo ni del derecho
		if(posNueva<0 || (posNueva+jugador.getAncho())>xMax){
			if(posNueva<0){
				//posNueva=-20;
				posNueva=0;
				jugador.setPosX(posNueva);

			}else{
				//posNueva=-20;
				posNueva=xMax-jugador.getAncho();
				jugador.setPosX(posNueva);
			}

		}else{
			if (((posJugadorCentro < (posNueva + rangoMovimiento)) && ((posNueva - rangoMovimiento) < posJugadorCentro))) {
				jugador.setPosX(posNueva);
			} else {
				if (jugador.getPosX() < posNueva) {
					//El bloque esta a la izquierda
					//verifico que si sumo la velocidad de transicion no supere la posicion que tiene que llegar
					if ((jugador.getPosX() + velocidadTransicion) < posNueva) {
						jugador.setPosX(jugador.getPosX()+velocidadTransicion);
					} else {
						jugador.setPosX(posNueva);
					}
				} else {
					//El bloque esta a la derecha
					if ((jugador.getPosX() - velocidadTransicion) > posNueva) {
						jugador.setPosX(jugador.getPosX()- velocidadTransicion);
					} else {
						jugador.setPosX(posNueva);
					}
				}
			}
		}

	}


	//La pelota verifica si toco algun borde de la pantalla
	public void verificarContactoPantalla() {

		//Derecha
		if (pelota.getX() + pelota.getTamanio() >= xMax) {
			pelota.setDireccionEnX(-pelota.getVelocidad());
		}

		//Izquierda
		if (pelota.getX() <= 0) {
			pelota.setDireccionEnX(pelota.getVelocidad());
		}

		//Fondo
		if (pelota.getY() + pelota.getTamanio() >= yMax) {
			pelota.setDireccionEnY(-pelota.getVelocidad());
			cayo=true;
		}

		//Techo
		if (pelota.getY() <= 120) {
			pelota.setDireccionEnY(pelota.getVelocidad());
		}

	}


	private void verificarContactoJugador2(){
		int areaDeContacto = this.jugador.getAreaDeContacto(this.pelota);
		this.pelota.setDireccion(areaDeContacto, "Jugador");
	}

	//Metodos de test!!!!!

	private void testPausarJuego(){
		boolean unContacto = false;
		int i=0;
		while (i<this.grilla.getCantidadFilas()&&! unContacto){
			int j=0;
			while (j<this.grilla.getCantidadColumnas()&&! unContacto){
				if(this.grilla.getBloque(i,j).getDureza()==1){
					Bloque bloque = this.grilla.getBloque(i,j);
					unContacto=verificarContacto2(bloque);
				}
				j++;
			}
			i++;
		}
	}

	public boolean verificarContacto2(Bloque b){
		boolean salida=false;
		int x=pelota.getPosSiguienteX();
		int y=pelota.getPosSiguienteY();
		int anchoPelota=pelota.getTamanio();
		int alto=pelota.getTamanio();

		Rect rec1=new Rect(x,y,x+anchoPelota,y+alto);
		int bX=(int)b.getPosX();
		int by=(int)b.getPosY();
		int bAncho=(int)(b.getPosX()+b.getAnchoBloque());
		int bAlto=(int)(b.getPosY()+b.getAltoBloque());
		Rect rec2=new Rect(bX, (int)b.getPosY(), (int)(b.getPosX()+b.getAnchoBloque()), (int)(b.getPosY()+b.getAltoBloque()));

		if(rec1.intersect(rec2)){

			controlarChoques();
			Paint colorChoque = new Paint();
			colorChoque.setColor(RED);
			b.setPincel(colorChoque);

			salida=true;
		}

		return salida;
	}

	public void controlarChoques(){
		gameThread.pause();//Para verificar contactos
		juegoEnPausa=true;
	}

	}
