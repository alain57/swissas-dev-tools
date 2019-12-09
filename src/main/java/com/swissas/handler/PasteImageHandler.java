package com.swissas.handler;

import java.awt.datatransfer.Transferable;

import com.intellij.diagnostic.PluginException;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorTextInsertHandler;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.util.Producer;
import com.swissas.ui.DragDropTextPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PasteImageHandler extends EditorActionHandler implements EditorTextInsertHandler {
	private static final Logger LOGGER = Logger.getInstance("Swiss-as");
	
	private final EditorActionHandler originalActionHandler;
	
	public PasteImageHandler(EditorActionHandler originalActionHandler){
		this.originalActionHandler = originalActionHandler;
	}
	
	@Override
	public void execute(Editor editor, DataContext dataContext, Producer<Transferable> producer) {
		Caret caret =  editor.getCaretModel().getPrimaryCaret();
		doExecute(editor, caret, dataContext);
	}
	
	@Override
	protected void doExecute(@NotNull Editor editor, @Nullable Caret caret,
	                         DataContext dataContext) {
		if(editor.getComponent() instanceof DragDropTextPane) {
			((DragDropTextPane) editor.getComponent()).pastePicture();
		}
		
		if (this.originalActionHandler != null) {
			//adapted the code like suggested here : https://intellij-support.jetbrains.com/hc/en-us/community/posts/115000438790-What-is-the-proper-way-to-delegate-paste-handler-execute-method-when-no-customization-is-needed-
			try {
				this.originalActionHandler.execute(editor, caret, dataContext);
			} catch (Throwable e) {
				PluginId pluginId;
				try {
					// this one is 2019(ish) only
					pluginId = PluginManagerCore
							.getPluginOrPlatformByClassName(this.originalActionHandler.getClass().getName());
				} catch (Throwable ignored) {
					// use old implementation, goes back to 2016
					pluginId = PluginManagerCore.getPluginByClassName(this.originalActionHandler.getClass().getName());
				}
				LOGGER.error(new PluginException("execute() delegated to original paste handler, " + e.getMessage(), e, pluginId));
			}
		}
	}
}
