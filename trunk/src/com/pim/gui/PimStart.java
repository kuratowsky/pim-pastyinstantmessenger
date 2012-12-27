/*******************************************************************************
 * Copyright (c) 2012 Benet Joan Darder.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Benet Joan Darder - initial API and implementation
 ******************************************************************************/
package com.pim.gui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.wb.swt.SWTResourceManager;

import rice.environment.Environment;

import com.pim.*;
import com.pim.canal.Canal;
import com.pim.scribe.PimScribeClient;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Es presenta tota l'aplicació amb una sola finestra però amb tota la funcionalitat.
 * 
 * Part esquerra la funcionalitat de nodes i creació de canals, separat per pestanyes. 
 * Part dreta la funcionalitat dels missatges, enviament i recepció.
 * 
 * @author Benet Joan Darder
 *
 */
public class PimStart {

	/**
	 * Shell que es basa l'aplicació
	 */
	protected Shell shlPimPastry;
	/**
	 * Taula amb la llista de canals creats
	 */
	private Table tbLlistaCanals;
	/**
	 * Camp de text per escriure el nom del canal a crear
	 */
	private Text txtNomCanal;
	/**
	 * Taula amb els nodes creats
	 */
	private Table tbNodes;
	/**
	 * Camp de text per escriure el num de nodes a crear
	 */
	private Text txtNumNodes;
	/**
	 * Camp de text per escriure el port a utilitzar
	 */
	private Text txtPortRemot;
	/**
	 * Camp de text per escriure la ip per arrencar el node
	 */
	private Text txtBootStap;
	/**
	 * Camp de text per escriure el port local
	 */
	private Text txtPortLocal;
	/**
	 * Camp de text per escriure el missatge a enviar
	 */
	private Text txtMissatge;
	/**
	 * Camp de text que veim els missatges enviats/rebuts entre nodes
	 */
	private Text txtMissatgesEntreNodes;
	/**
	 * Taula amb els canals subscrits d'un node seleccionat de la taula de nodes
	 */
	private Table tbCanalsSubscrits;
	/**
	 * Desplegable de nodes de destí del missatge privat
	 */
	private Combo cbNodesPerEnviarMissatge;
	/**
	 * Desplegable de canals per enviar un missatge a tots els seus subscriptors
	 */
	private Combo cbCanalsPerEnviarMissatge;
	/**
	 * Desplegable de canals diponibles per subscriure-s'hi
	 */
	private Combo cbCanalsDisponibles; 
	
	/**
	 * Integer emb el valor del primer port lliure de la màquina
	 */
	private int port;
	
	/**
	 * Botó que envia les dades dels caps de texte al constructor PIM
	 */
	private Button btCrearNodes;
	/**
	 * Objecte PIM amb la funcionalitats de manipulació de nodes
	 */
	private PIM pim = null;
	
	/**
	 * Objecte Medi de tipus rice.environment.Environment
	 */
	private Environment env;
	
	/**
	 * Vector amb els canals creats a la pestanya de creació de canals
	 */
	private Vector<Canal> nousCanals = new Vector<Canal>(); 
	
	
	
	/**
	 * Llança l'aplicació.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PimStart window = new PimStart();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Obre la finestra de l'aplicació.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlPimPastry.open();
		shlPimPastry.layout();
		while (!shlPimPastry.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Crea els continguts de la finestra d'aplicació.
	 */
	protected void createContents() {
		shlPimPastry = new Shell(SWT.ON_TOP | SWT.CLOSE | SWT.TITLE);//els parametres d'estil que li passam al constructor, fa que no sigui possible fer la finestra més gran o més petita.
		shlPimPastry.setSize(1039, 584);
		shlPimPastry.setText("Pim! - Pastry Instant Messenger");
		shlPimPastry.setLayout(null);
		/**
		 * Quan tanquem l'aplicació amollem tots els recursos.
		 * Eliminem tots els nodes i sortim. 
		 */
		shlPimPastry.addShellListener(new ShellAdapter(){
			public void shellClosed(ShellEvent e){
				if (pim != null){
					pim.destroyAllNodes();
				}
				shlPimPastry.getDisplay().dispose();
				System.exit(0);
			}
		});

		TabFolder tabFolder = new TabFolder(shlPimPastry, SWT.NONE);
		tabFolder.setBounds(10, 10, 628, 537);

		TabItem tbtmNodes = new TabItem(tabFolder, SWT.NONE);
		tbtmNodes.setText("Nodes");

		Composite compositeNodes = new Composite(tabFolder, SWT.NONE);
		tbtmNodes.setControl(compositeNodes);

		cbCanalsDisponibles = new Combo(compositeNodes, SWT.NONE);
		cbCanalsDisponibles.setBounds(409, 84, 127, 21);

		Label lblCanalsDisponibles = new Label(compositeNodes, SWT.NONE);
		lblCanalsDisponibles.setBounds(311, 87, 92, 13);
		lblCanalsDisponibles.setText("Canals disponibles");

		Button btnEliminarNode = new Button(compositeNodes, SWT.NONE);
		btnEliminarNode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(pim != null){
					int ndindex = tbNodes.getSelectionIndex();
					if(ndindex != -1){
						pim.deleteAppByIndex(ndindex);
						Vector<PimScribeClient> v = pim.getApps();
						alertWindow(SWT.ICON_INFORMATION, "Correcte", "Node eliminat correctament");
						fillNodeTable(tbNodes, v);
						fillNodesCombo(cbNodesPerEnviarMissatge, v);
					}else{
						alertWindow(SWT.ICON_INFORMATION, "Selecciona Node", "Primer ha de seleccionar un node de la graella de nodes");
						tbNodes.setFocus();
					}
				}else{
					alertWindow(SWT.ICON_INFORMATION, "No hi ha nodes", "No es pot eliminar cap node, ja que no existeix cap node");
				}
			}
		});
		btnEliminarNode.setBounds(10, 84, 83, 23);
		btnEliminarNode.setText("Eliminar Node");

		Button btnEntrarCanal = new Button(compositeNodes, SWT.NONE);
		btnEntrarCanal.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(pim != null){
					int ndindex = tbNodes.getSelectionIndex();
					if(ndindex != -1){
						PimScribeClient app = pim.getAppByIndex(ndindex);
						String nomCanal = cbCanalsDisponibles.getText();
						if(nomCanal.length()>0){
							subscribeToChannel(app, nomCanal);
							fillChannelsTable(tbCanalsSubscrits,app.getCanalsSubscrits());
							fillMessagePanel(app.getMissatges());
						}else{
							alertWindow(SWT.ICON_INFORMATION, "Selecciona Canal", "Primer ha de seleccionar un canal per subscriure el node");
							cbCanalsDisponibles.setFocus();
						}
					}else{
						alertWindow(SWT.ICON_INFORMATION, "Selecciona Node", "Primer ha de seleccionar un node de la graella de nodes");
						tbNodes.setFocus();
					}
				}else{
					alertWindow(SWT.ICON_INFORMATION, "No hi ha nodes", "No es pot eliminar cap node, ja que no existeix cap node");
				}
			}
		});
		btnEntrarCanal.setBounds(542, 82, 68, 23);
		btnEntrarCanal.setText("Entrar");

		ScrolledComposite sctbNodes = new ScrolledComposite(compositeNodes, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		sctbNodes.setBounds(10, 132, 600, 151);
		sctbNodes.setExpandHorizontal(true);
		sctbNodes.setExpandVertical(true);

		tbNodes = new Table(sctbNodes, SWT.BORDER | SWT.FULL_SELECTION);
		tbNodes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PimScribeClient app = pim.getAppByIndex(tbNodes.getSelectionIndex());
				fillChannelsTable(tbCanalsSubscrits,app.getCanalsSubscrits());
				txtMissatgesEntreNodes.setText("");
				txtMissatgesEntreNodes.setText(app.getMissatges());
			}
		});
		tbNodes.setHeaderVisible(true);
		tbNodes.setLinesVisible(true);

		TableColumn tbcIdNode = new TableColumn(tbNodes, SWT.NONE);
		tbcIdNode.setWidth(556);
		tbcIdNode.setText("Id Node");
		
		sctbNodes.setContent(tbNodes);
		sctbNodes.setMinSize(tbNodes.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Label lblLlistatDeNodes = new Label(compositeNodes, SWT.NONE);
		lblLlistatDeNodes.setBounds(10, 113, 83, 13);
		lblLlistatDeNodes.setText("Llistat de nodes");

		Group grpDadesDeConnexi = new Group(compositeNodes, SWT.NONE);
		grpDadesDeConnexi.setText("Dades de connexi\u00F3");
		grpDadesDeConnexi.setBounds(10, 10, 600, 66);

		try {
			port = new ServerSocket(0).getLocalPort();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		txtPortLocal = new Text(grpDadesDeConnexi, SWT.BORDER);
		txtPortLocal.setBounds(68, 25, 47, 19);
		txtPortLocal.setText(String.valueOf(port));

		txtBootStap = new Text(grpDadesDeConnexi, SWT.BORDER);
		txtBootStap.setBounds(179, 25, 112, 19);
		try {
			txtBootStap.setText(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}


		txtPortRemot = new Text(grpDadesDeConnexi, SWT.BORDER);
		txtPortRemot.setBounds(361, 25, 47, 19);
		txtPortRemot.setText(String.valueOf(port));

		txtNumNodes = new Text(grpDadesDeConnexi, SWT.BORDER);
		txtNumNodes.setBounds(485, 25, 31, 19);

		btCrearNodes = new Button(grpDadesDeConnexi, SWT.NONE);
		btCrearNodes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(txtPortLocal.getText().length() > 0 && txtBootStap.getText().length() > 0 && txtPortRemot.getText().length() > 0 && txtNumNodes.getText().length() > 0){
					pim = creaPim();
					Vector<PimScribeClient> v = pim.getApps();
					fillNodeTable(tbNodes, v);
					fillNodesCombo(cbNodesPerEnviarMissatge, v);
				}else if(txtPortLocal.getText().length() == 0){
					alertWindow(SWT.ICON_WARNING, "Camp obligatori!", "El camp Port Local, és obligatòri");
					txtPortLocal.setFocus();
				}else if(txtBootStap.getText().length() == 0){
					alertWindow(SWT.ICON_WARNING, "Camp obligatori!", "El camp bootstrap, és obligatòri");
					txtBootStap.setFocus();
				}else if(txtPortRemot.getText().length() == 0){
					alertWindow(SWT.ICON_WARNING, "Camp obligatori!", "El camp Port remot, és obligatòri");
					txtPortRemot.setFocus();
				}else if(txtNumNodes.getText().length() == 0){
					alertWindow(SWT.ICON_WARNING, "Camp obligatori!", "El camp Num. Nodes, és obligatòri");
					txtNumNodes.setFocus();
				}
			}
		});
		btCrearNodes.setText("Crear Nodes");
		btCrearNodes.setBounds(522, 25, 68, 23);

		Label lbNumNodes = new Label(grpDadesDeConnexi, SWT.NONE);
		lbNumNodes.setText("Num. Nodes");
		lbNumNodes.setBounds(413, 28, 66, 13);

		Label lbPortRemot = new Label(grpDadesDeConnexi, SWT.NONE);
		lbPortRemot.setText("Port Remot:");
		lbPortRemot.setBounds(297, 28, 58, 13);

		Label lbBootStrap = new Label(grpDadesDeConnexi, SWT.NONE);
		lbBootStrap.setText("BootStrap:");
		lbBootStrap.setBounds(121, 28, 52, 13);

		Label lbPortLocal = new Label(grpDadesDeConnexi, SWT.NONE);
		lbPortLocal.setText("Port Local:");
		lbPortLocal.setBounds(10, 28, 52, 13);

		ScrolledComposite sctbCanalsSubscrits = new ScrolledComposite(compositeNodes, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		sctbCanalsSubscrits.setBounds(9, 339, 601, 162);
		sctbCanalsSubscrits.setExpandHorizontal(true);
		sctbCanalsSubscrits.setExpandVertical(true);

		tbCanalsSubscrits = new Table(sctbCanalsSubscrits, SWT.BORDER | SWT.FULL_SELECTION);
		tbCanalsSubscrits.setHeaderVisible(true);
		tbCanalsSubscrits.setLinesVisible(true);

		TableColumn tbcTopicSubscrit = new TableColumn(tbCanalsSubscrits, SWT.NONE);
		tbcTopicSubscrit.setWidth(260);
		tbcTopicSubscrit.setText("Topic");

		TableColumn tbcNomSubscrit = new TableColumn(tbCanalsSubscrits, SWT.NONE);
		tbcNomSubscrit.setWidth(258);
		tbcNomSubscrit.setText("Nom");
		sctbCanalsSubscrits.setContent(tbCanalsSubscrits);
		sctbCanalsSubscrits.setMinSize(tbCanalsSubscrits.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Label lblCanalsSubscrits = new Label(compositeNodes, SWT.NONE);
		lblCanalsSubscrits.setBounds(10, 320, 83, 13);
		lblCanalsSubscrits.setText("Canals subscrits");

		TabItem tbtmCanals = new TabItem(tabFolder, SWT.NONE);
		tbtmCanals.setText("Canals");

		Composite compositeCanals = new Composite(tabFolder, SWT.NONE);
		tbtmCanals.setControl(compositeCanals);

		ScrolledComposite scrolledComposite = new ScrolledComposite(compositeCanals, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBounds(10, 65, 600, 318);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		tbLlistaCanals = new Table(scrolledComposite, SWT.BORDER | SWT.FULL_SELECTION);
		tbLlistaCanals.setHeaderVisible(true);
		tbLlistaCanals.setLinesVisible(true);

		TableColumn tbcTopic = new TableColumn(tbLlistaCanals, SWT.NONE);
		tbcTopic.setWidth(276);
		tbcTopic.setText("Topic");

		TableColumn tbcNomDelCanal = new TableColumn(tbLlistaCanals, SWT.NONE);
		tbcNomDelCanal.setWidth(233);
		tbcNomDelCanal.setText("Nom del canal");
		scrolledComposite.setContent(tbLlistaCanals);
		scrolledComposite.setMinSize(tbLlistaCanals.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		txtNomCanal = new Text(compositeCanals, SWT.BORDER);
		txtNomCanal.setBounds(90, 10, 202, 19);

		Button btnAltaCanal = new Button(compositeCanals, SWT.NONE);
		btnAltaCanal.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				nousCanals.add(new Canal(txtNomCanal.getText(),env));
				fillChannelsTable(tbLlistaCanals, nousCanals);
				fillChannelsCombo(cbCanalsDisponibles, nousCanals);
				fillChannelsCombo(cbCanalsPerEnviarMissatge, nousCanals);
			}
		});
		btnAltaCanal.setBounds(298, 8, 68, 23);
		btnAltaCanal.setText("Alta Canal");

		Label lblNom = new Label(compositeCanals, SWT.NONE);
		lblNom.setBounds(10, 13, 74, 13);
		lblNom.setText("Nom del canal:");

		Label lblLlistatDeCanals = new Label(compositeCanals, SWT.NONE);
		lblLlistatDeCanals.setBounds(10, 46, 113, 13);
		lblLlistatDeCanals.setText("Llistat de canals");

		Group grpMissatges = new Group(shlPimPastry, SWT.NONE);
		grpMissatges.setText("Missatges");
		grpMissatges.setBounds(644, 10, 377, 537);

		txtMissatge = new Text(grpMissatges, SWT.BORDER);
		txtMissatge.setBounds(10, 42, 357, 19);

		Button btnEnviarMissatgeNode = new Button(grpMissatges, SWT.NONE);
		btnEnviarMissatgeNode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (pim !=null){
					Vector<PimScribeClient> v = pim.getApps();					
					//missatge a enviar
					String txt = txtMissatge.getText();
					int ndFromindex = tbNodes.getSelectionIndex();
					int ndToindex = cbNodesPerEnviarMissatge.getSelectionIndex();
					//canal a enviar
					if(txt.length()!=0){						
						if(ndFromindex > -1 && ndToindex > -1){
							PimScribeClient appFrom = v.get(ndFromindex);
							PimScribeClient appTo = v.get(ndToindex);
							if (appFrom != appTo){
								//txtMissatgesEntreNodes.setText(appFrom.routeMyMsgDirect(appTo.getEndPoint().getLocalNodeHandle(), txt));
								txtMissatgesEntreNodes.setText(appFrom.routeMyMsgDirect(appTo.getEndPoint().getId(), txt));
							}else{
								alertWindow(SWT.ICON_WARNING, "Selecciona Node", "El node de destí ha de ser diferent al d'origen");	
							}
						}else if(ndFromindex == -1){
							alertWindow(SWT.ICON_INFORMATION, "Selecciona Node", "Ha de seleccionar un node origen, de la graella de nodes");
							cbNodesPerEnviarMissatge.setFocus();
						} else if(ndToindex == -1){
							alertWindow(SWT.ICON_INFORMATION, "Selecciona Node", "Ha de seleccionar un node deestí del desplegable de nodes");
							tbNodes.setFocus();
						}
					}else{
						alertWindow(SWT.ICON_INFORMATION, "Missatge buid!", "Es necessari que el camp missatge tingui contingut");
						txtMissatge.setFocus();
					}
				}else{
					alertWindow(SWT.ICON_INFORMATION, "No hi ha nodes ni canals", "No es pot enviar cap missatge, ja que no existeixen nodes ni canals");
				}
			}
		});
		btnEnviarMissatgeNode.setBounds(170, 88, 81, 23);
		btnEnviarMissatgeNode.setText("Enviar al node");

		cbNodesPerEnviarMissatge = new Combo(grpMissatges, SWT.NONE);
		cbNodesPerEnviarMissatge.setBounds(10, 90, 154, 21);

		Button btnEnviarMissatgeCanal = new Button(grpMissatges, SWT.NONE);
		btnEnviarMissatgeCanal.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (pim !=null){
					Vector<PimScribeClient> v = pim.getApps();
					Canal c = new Canal();
					//missatge a enviar
					String txt = txtMissatge.getText();
					int ndindex = tbNodes.getSelectionIndex();
					int canindex = cbCanalsPerEnviarMissatge.getSelectionIndex();
					//canal a enviar
					if(txt.length()!=0){						
						if(canindex > -1 && ndindex > -1){
							c = nousCanals.get(canindex);
							PimScribeClient app = v.get(ndindex);
							txtMissatgesEntreNodes.setText(app.sendMulticast(c, txt));
						}else if(canindex == -1){
							alertWindow(SWT.ICON_INFORMATION, "Selecciona Canal", "Ha de seleccionar un canal del desplegable de canals");
							cbCanalsPerEnviarMissatge.setFocus();
						} else if(ndindex == -1){
							alertWindow(SWT.ICON_INFORMATION, "Selecciona Node", "Primer ha de seleccionar un node de la graella de nodes");
							tbNodes.setFocus();
						}
					}else{
						alertWindow(SWT.ICON_INFORMATION, "Missatge buid!", "Es necessari que el camp missatge tingui contingut");
						txtMissatge.setFocus();
					}
				}else{
					alertWindow(SWT.ICON_INFORMATION, "No hi ha nodes ni canals", "No es pot enviar cap missatge, ja que no existeixen nodes ni canals");
				}
			}
		});
		btnEnviarMissatgeCanal.setBounds(170, 115, 81, 23);
		btnEnviarMissatgeCanal.setText("Enviar al canal");

		cbCanalsPerEnviarMissatge = new Combo(grpMissatges, SWT.NONE);		
		cbCanalsPerEnviarMissatge.setBounds(10, 117, 154, 21);

		txtMissatgesEntreNodes = new Text(grpMissatges, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txtMissatgesEntreNodes.setBounds(10, 164, 357, 298);

		Label lblPerBenetJoan = new Label(grpMissatges, SWT.NONE);
		lblPerBenetJoan.setFont(SWTResourceManager.getFont("Tahoma", 15, SWT.NORMAL));
		lblPerBenetJoan.setBounds(38, 468, 306, 23);
		lblPerBenetJoan.setText("Per: Benet Joan Darder Canyelles");

		Label lblUocUniversitat = new Label(grpMissatges, SWT.NONE);
		lblUocUniversitat.setBounds(96, 497, 196, 13);
		lblUocUniversitat.setText("UOC - Universitat Oberta de Catalunya");

		Label lblerSemestreCurs = new Label(grpMissatges, SWT.NONE);
		lblerSemestreCurs.setBounds(116, 516, 154, 13);
		lblerSemestreCurs.setText("1er Semestre curs 2012-2013");

		Label lblEscriuUnMissatge = new Label(grpMissatges, SWT.NONE);
		lblEscriuUnMissatge.setBounds(10, 23, 99, 13);
		lblEscriuUnMissatge.setText("Escriu un missatge:");

		Label lblSeleccionaAQui = new Label(grpMissatges, SWT.NONE);
		lblSeleccionaAQui.setBounds(10, 71, 154, 13);
		lblSeleccionaAQui.setText("Selecciona a qui ho vols enviar:");

		Label lblMissatgesRebuts = new Label(grpMissatges, SWT.NONE);
		lblMissatgesRebuts.setBounds(10, 145, 99, 13);
		lblMissatgesRebuts.setText("Missatges rebuts:");
		
		Button btnSortirDelCanal = new Button(compositeNodes, SWT.NONE);
		btnSortirDelCanal.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(pim != null){
					int ndindex = tbNodes.getSelectionIndex();
					int cindex = tbCanalsSubscrits.getSelectionIndex();
					if(ndindex != -1 && cindex > 0){
						PimScribeClient app = pim.getAppByIndex(ndindex);
						Vector<Canal> c = app.getCanalsSubscrits();
						String canalSortida = c.get(cindex).getName();
						app.unsubscribe(canalSortida);
						alertWindow(SWT.ICON_INFORMATION, "Correcte", "Has sortit correctament del canal " + canalSortida);
						fillChannelsTable(tbCanalsSubscrits, app.getCanalsSubscrits());						
					}else if(cindex == 0){
						alertWindow(SWT.ICON_WARNING, "Aquest canal no!", "Aquest canal es per tothom. No pots fogir d'ell");
						tbNodes.setFocus();
					}else if (ndindex==-1){
						alertWindow(SWT.ICON_INFORMATION, "Selecciona Node", "Primer ha de seleccionar un node de la graella de nodes");
						tbNodes.setFocus();
					}else{
						alertWindow(SWT.ICON_INFORMATION, "Selecciona canal subscrit", "Primer ha de seleccionar un canal per poder sortir d'ell");
						tbCanalsSubscrits.setFocus();
					}
				}else{
					alertWindow(SWT.ICON_INFORMATION, "No hi ha nodes", "No es pot fer res, no existeix cap node");
				}
			}
		});
		btnSortirDelCanal.setBounds(10, 289, 92, 23);
		btnSortirDelCanal.setText("Sortir del Canal");
		
		/**
		 * Canal base. Tots els nodes Scribe estaran subscrits a aquest canal
		 */
		nousCanals.add(new Canal("PIM_canal_base",env));
		fillChannelsTable(tbLlistaCanals, nousCanals);
		fillChannelsCombo(cbCanalsDisponibles,nousCanals);		
		fillChannelsCombo(cbCanalsPerEnviarMissatge,nousCanals);
	}

	/**
	 * Crea un objecte PIM.
	 * Crida el constructor de PIM que crea els nodes, crea l'anell i subscriu els nodes a un canal per defecte.
	 * 
	 * L'objecte resultant, ens permetrá usar els mètodes de la classe PIM
	 * 
	 * @return objecte PIM
	 */
	protected PIM creaPim(){		
		try {
			env = new Environment();
			env.getParameters().setString("nat_search_policy","never");
			int bindport = Integer.parseInt(txtPortLocal.getText());		
			// build the bootaddress from the command line args
			InetAddress bootaddr = InetAddress.getByName(txtBootStap.getText());
			int bootport = Integer.parseInt(txtPortRemot.getText());
			InetSocketAddress bootaddress = new InetSocketAddress(bootaddr, bootport);
			// the port to use locally
			int numNodes = Integer.parseInt(txtNumNodes.getText());
			return new PIM(bindport, bootaddress, numNodes, env, nousCanals.get(0));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}		
	}

	/**
	 * Omple la taula amb els els nodes Scribe
	 * 
	 * @param tb Taula
	 * @param obj vector amb els nodes Scribe
	 */
	protected void fillNodeTable(Table tb, Vector<PimScribeClient> obj){
		tb.removeAll();
		tb.setRedraw(false);
		for(Iterator<PimScribeClient> it = obj.iterator(); it.hasNext();){
			PimScribeClient psc = it.next();
			int c = 0;
			TableItem item = new TableItem(tb, SWT.NONE);
			item.setText(c++, psc.getEndPoint().getId().toStringFull());
		}
		tb.setRedraw(true);
		btCrearNodes.setEnabled(false);
	}

	/**
	 * Omple el desplegable per seleccionar un node a enviar-li un missatge
	 * @param cb combo a omplir
	 * @param obj objecte amb el llistat de nodes
	 */	
	protected void fillNodesCombo(Combo cb, Vector<PimScribeClient> obj){
		cb.removeAll();
		cb.setRedraw(false);
		for (int i = 0; i < obj.size(); i++) {
			cb.add((obj.get(i)).getEndPoint().getId().toString());
			if(i==0)cb.select(i);
		}
		cb.setRedraw(true);
	}

	/**
	 * Omple la taula amb els canals que s'han subscrits, els nodes seleccionats a la taula de nodes
	 *  
	 * @param tb taula de relació de canals - nodes
	 * @param obj Vector amb els canals 
	 */	
	protected void fillChannelsTable(Table tb, Vector<Canal> obj) {
		tb.removeAll();
		tb.setRedraw(false);
		for(Iterator<Canal> it = obj.iterator(); it.hasNext();){
			Canal canal = it.next();
			int c = 0;
			TableItem item = new TableItem(tb, SWT.NONE);			
			item.setText(c++, canal.getTopic().toString());
			item.setText(c++, canal.getName());
		}
		tb.setRedraw(true);		
	}

	/**
	 * Omple el desplegable per seleccionar un node a enviar-li un missatge
	 * 
	 * @param cb objecte desplegable
	 * @param obj objecte amb el que hem d'omplir el desplegable
	 */	
	protected void fillChannelsCombo (Combo cb, Vector<Canal> obj){
		cb.removeAll();
		cb.setRedraw(false);
		for (int i = 0; i < obj.size(); i++) {
			cb.add((obj.get(i)).getName());
			if(i==0)cb.select(i);
		}
		cb.setRedraw(true);
	}

	/**
	 * Métode que, donat un node seleccionat de la taula de nodes, i un canal seleccionat del desplegable de canals, subscriu el node al canal.
	 * 
	 * @param app Node que volem subscriure
	 * @param canal Canal al que volem subscriure
	 */
	protected void subscribeToChannel(PimScribeClient app, String canal) {
		boolean subscrit = app.subscribe(canal, env);
		if(subscrit){
			alertWindow(SWT.ICON_INFORMATION, "Canal subscrit", "S'ha subscrit correctament al canal "+ canal);
		}else{
			alertWindow(SWT.ICON_WARNING, "Ja estava subscrit", "No s'ha subscrit al canal "+ canal+", ja hi estava subscrit!");
		}		
	}

	/**
	 * Mètode que mostra al camp de text els missatges enviats i rebuts segons el node seleccionat de la taula de nodes
	 * 
	 * @param missatges missatges a mostrar
	 */
	protected void fillMessagePanel(String missatges) {
		txtMissatgesEntreNodes.setText("");
		txtMissatgesEntreNodes.setText(missatges);
	}

	/**
	 * Mètode que mostra la finestra d'alerta segons els paràmetres que reb
	 * 
	 * @param typeIcon tipus d'icona segons el tipus d'alerta
	 * @param title Títol de la finestra
	 * @param message Missatge
	 */
	protected void alertWindow(int typeIcon, String title, String message){
		MessageBox win = new MessageBox(shlPimPastry, typeIcon|SWT.ABORT);
		win.setMessage(message);
		win.setText(title);
		win.open();
	}
}
