package com.zizibujuan.niubizi.client.ui;


import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.zizibujuan.niubizi.client.ui.events.ItemChangedListener;
import com.zizibujuan.niubizi.server.model.CategoryInfo;
import com.zizibujuan.niubizi.server.model.FileInfo;
import com.zizibujuan.niubizi.server.model.FileTag;
import com.zizibujuan.niubizi.server.model.TagInfo;
import com.zizibujuan.niubizi.server.model.UserInfo;
import com.zizibujuan.niubizi.server.service.CategoryService;
import com.zizibujuan.niubizi.server.service.FileService;
import com.zizibujuan.niubizi.server.service.TagService;
import com.zizibujuan.niubizi.server.service.UserService;

/**
 * 补充文件信息的窗口
 * 
 * @author jinzw
 * @since 0.0.1
 */
public class FileImportWindow {
	
	private final class ToggleTagListener implements MouseListener {
		@Override
		public void mouseUp(MouseEvent e) {
			System.out.println(e);
			Label lbl = (Label)e.getSource();
			Composite c = lbl.getParent();
			Boolean selected = false;
			Object o = c.getData("selected");
			if(o != null){
				selected = Boolean.valueOf(o.toString());
			}

			Control[] children = c.getChildren();
			Label lblDuihao = (Label) children[0];
			Label lblRemove = (Label)children[2];
			
			FileTag ft = new FileTag();
			ft.setFileId(fileInfo.getId());
			TagInfo tag = (TagInfo) c.getData("tagInfo");
			ft.setTagId(tag.getId());
			
			if(selected){
				lblDuihao.setImage(null);
				lblRemove.setImage(null);
				fileService.removeTag(ft);
			}else{
				ImageData imgDuihaoData = new ImageData(getClass().getResourceAsStream("/icons/iconfont-duihao.png"));
				lblDuihao.setImage(new Image(window.getDisplay(), imgDuihaoData));
				
				ImageData imgRemoveData = new ImageData(getClass().getResourceAsStream("/icons/iconfont-shanchu.png"));
				lblRemove.setImage(new Image(window.getDisplay(), imgRemoveData));
				
				fileService.addTag(ft);
			}
			c.setData("selected", !selected);
			
		}

		@Override
		public void mouseDown(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}

	private Shell window;
	
	// 文件完整路径
	private String filePath;
	
	// 控件
	private Label lblFileIcon;
	private Label lblFileName;
	
	private Text txtFileName;
	private Label lblNameExample;
	
	private Combo cbCategory;
	private Combo cbSender;
	private Composite tagContainer;
	
	// 服务
	private FileService fileService = ServiceHolder.getDefault().getFileService();
	private TagService tagService = ServiceHolder.getDefault().getTagService();
	private CategoryService categoryService = ServiceHolder.getDefault().getCategoryService();
	private UserService userService = ServiceHolder.getDefault().getUserService();
	
	// 默认数据
	private FileInfo fileInfo; // 这个模块主要维护这个model对象
	private List<UserInfo> senders;
	private List<CategoryInfo> categories;
	private boolean fileNameIsUsed = false;
	
	
	public FileImportWindow(){
		window = new Shell(SWT.CLOSE | SWT.ON_TOP);
		window.setSize(300, 600);
        // TODO: 计算相对位置
		int x = 150;
		int y = 100;
		window.setLocation(x, y);
	        
		createWindowContent();
	}
	
	/**
	 * 创建窗体内容
	 */
	private void createWindowContent(){
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		window.setLayout(layout);
		// window.setBackground(new Color(null, 200,100,100));
		
		// 文档基本信息
		//Composite cpFileBaseInfo = new Composite(window, SWT.NULL);
		lblFileIcon = new Label(window, SWT.NULL);
		GridData gdFileIcon = new GridData();
		gdFileIcon.widthHint = 32;
		gdFileIcon.heightHint = 32;
		lblFileIcon.setLayoutData(gdFileIcon);
		
		lblFileName = new Label(window, SWT.WRAP);
		lblFileName.setBackground(window.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridData gdFileName = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		gdFileName.widthHint = 250;
		gdFileName.heightHint = 32;
		lblFileName.setLayoutData(gdFileName);
		
		// 文档分类
		Label lblCategory = new Label(window, SWT.NULL);
		lblCategory.setText("文档分类");
		GridData gdCategoryLabel = new GridData();
		lblCategory.setLayoutData(gdCategoryLabel);
		
		Button btnAddCategory = new Button(window, SWT.PUSH);
		btnAddCategory.setImage(NBZUtils.getImage(window, "iconfont-xinzeng16_16.png"));
		btnAddCategory.setText("新增分类");
		btnAddCategory.setLayoutData(new GridData());
		btnAddCategory.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CategorySettingWindow categorySettingWindow = new CategorySettingWindow();
				categorySettingWindow.addItemChangedListener(new ItemChangedListener() {
					@Override
					public void itemChanged() {
						cbCategory.removeAll();
						categories = categoryService.get();
						cbCategory.add(NBZ.UNDEFINED_VALUE);
						for(CategoryInfo c : categories){
							cbCategory.add(c.getName());
						}
						cbCategory.select(0);
					}
				});
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		cbCategory = new Combo(window, SWT.READ_ONLY);
		categories = categoryService.get();
		cbCategory.add(NBZ.UNDEFINED_VALUE);
		for(CategoryInfo c : categories){
			cbCategory.add(c.getName());
		}
		cbCategory.select(0);
		
		GridData gdCategory = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdCategory.horizontalSpan = 2;
		cbCategory.setLayoutData(gdCategory);
		
		// 内部处理，可以在获得焦点时，保存原来的数据;
		// 在失去焦点时，做判断，如果发生了数据变化，则进行相应的处理即可
		
		// 规范后的名称
		Label lblRename = new Label(window, SWT.NULL);
		lblRename.setText("规范名称");
		GridData gdRenameLabel = new GridData();
		gdRenameLabel.horizontalSpan = 2;
		lblRename.setLayoutData(gdRenameLabel);
		
		txtFileName = new Text(window, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		GridData gdRename = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdRename.horizontalSpan = 2;
		gdRename.heightHint = 32;
		txtFileName.setLayoutData(gdRename);
		

		// 命名示例，与文档类型挂钩
		lblNameExample = new Label(window, SWT.NULL);
		GridData gdNameExample = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdNameExample.horizontalSpan = 2;
		lblNameExample.setLayoutData(gdNameExample);
		
		// 发送人标签
		Label lblSender = new Label(window, SWT.NULL);
		lblSender.setText("发送人");
		GridData gdSenderLabel = new GridData();
		gdSenderLabel.horizontalSpan = 2;
		lblSender.setLayoutData(gdSenderLabel);
		
		// 常选中的人放在上面
		cbSender = new Combo(window, SWT.READ_ONLY);
		cbSender.add(NBZ.UNDEFINED_VALUE);
		senders = userService.getUsersOrderByDisplayName(); // TODO:与机构挂钩，给机构一个简写，显示名为"西南-张三"
		for(UserInfo c : senders){
			cbSender.add(c.getDisplayName());
		}
		cbSender.select(0);
		GridData gdSender = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdSender.horizontalSpan = 2;
		cbSender.setLayoutData(gdSender);
		
		// 显示关键字
		Label lblKeywords = new Label(window, SWT.NULL);
		lblKeywords.setText("关键字");
		GridData gdlblKeywordsLabel = new GridData();
		lblKeywords.setLayoutData(gdlblKeywordsLabel);
		
		Button btnAddKeywords = new Button(window, SWT.PUSH);
		btnAddKeywords.setImage(NBZUtils.getImage(window, "iconfont-xinzeng16_16.png"));
		btnAddKeywords.setText("新增关键字");
		btnAddKeywords.setLayoutData(new GridData());
		btnAddKeywords.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO： 单例对象？
				TagsSettingWindow settingWindow = new TagsSettingWindow();
				// 同步标签
				settingWindow.addItemChangedListener(new ItemChangedListener() {
					@Override
					public void itemChanged() {
						if(tagContainer != null){
							refreshTagList(tagContainer);
						}
					}
				});
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		tagContainer = new Composite(window, SWT.FILL);
		RowLayout rwTagContainer = new RowLayout(SWT.VERTICAL);
		tagContainer.setLayout(rwTagContainer);
		GridData gdlblKeyWords = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdlblKeyWords.horizontalSpan = 2;
		tagContainer.setLayoutData(gdlblKeyWords);
		
		refreshTagList(tagContainer);
	}
	
	private void onDataChanged() {
		fileService.update(fileInfo);
	}

	private void refreshTagList(Composite container) {
		Control[] children = container.getChildren();
		for(Control c : children){
			c.dispose();
		}
		
		java.util.List<TagInfo> tags = tagService.get();
		for(TagInfo tag : tags){
			Composite cpTagContainer = new Composite(container, SWT.BORDER);
			RowData rd = new RowData(200, 20);
			cpTagContainer.setLayoutData(rd);
			cpTagContainer.setData("tagInfo", tag);
			cpTagContainer.setLayout(new RowLayout());
			
			Label lblSelected = new Label(cpTagContainer, SWT.NULL);
			RowData rdSelected = new RowData(16, 16);
			lblSelected.setLayoutData(rdSelected);
			lblSelected.setCursor(new Cursor(window.getDisplay(), SWT.CURSOR_HAND));
			
			Label lblTagName = new Label(cpTagContainer, SWT.NULL);
			lblTagName.setText(tag.getName());
			RowData rdTagName = new RowData(150, 16);
			lblTagName.setLayoutData(rdTagName);
			lblTagName.setCursor(new Cursor(window.getDisplay(), SWT.CURSOR_HAND));
			
			Label lblDelete = new Label(cpTagContainer, SWT.NULL);
			lblDelete.setSize(16, 16);
			RowData rdDelete = new RowData(16, 16);
			lblDelete.setLayoutData(rdDelete);
			lblDelete.setCursor(new Cursor(window.getDisplay(), SWT.CURSOR_HAND));
			
			lblSelected.addMouseListener(new ToggleTagListener());
			lblTagName.addMouseListener(new ToggleTagListener());
			lblDelete.addMouseListener(new ToggleTagListener());
		}
		container.pack();
	}
	
	public void show(String... filePathArray){
		
		if(!window.isVisible()){
			window.open();
		}
		// 第一版本，只支持一次拖动一个文件。
		if(filePathArray.length == 0){
			return;
		}
		// 文件类型对应的图标和文件名称
		filePath = filePathArray[0];
		String fileType = FilenameUtils.getExtension(filePath);
		String fileName = FilenameUtils.getBaseName(filePath);
		lblFileIcon.setImage(NBZUtils.getFileIcon(fileType));
		lblFileName.setText(fileName);
		
		// 可输入新文件名
		txtFileName.setText(fileName);
		txtFileName.setSelection(fileName.length(), fileName.length());
		// 名称规范
		lblNameExample.setText("xxxx-xx-xxx");
		
		// 存储文件路径，文件名，保存时间
		
		// 怎么判断文件已存在？先通过文件路径判断
		// 如果文件已存在，则加载出已存在文件的信息
		// 如果文件没有存在，则新增文件
		fileInfo = new FileInfo();
		fileInfo.setFilePath(filePath);
		fileInfo.setFileName(fileName);
		fileInfo.setFileType(fileType);
		fileInfo.setCreateTime(new Date());
		
		// TODO:解析文件名是否符合规范
		
		// 复制文件
		File managedDir = NBZUtils.getManagedDir();
		// 先判断同名的文件是否已存在，如果存在则提示用户修改文件名，不能重名。
		try {
			
			File destFile = new File(managedDir, new File(filePath).getName());
			if(destFile.exists()){
				// 临时注释，第一版不处理文件重命名的问题，需要用户在程序外处理，这里只给出提示。
				// 可以做这样一个变通，将移动文件放在重命名文件后
//				String uuid = UUID.randomUUID().toString();
//				File unTrackedDir = new File(destDirString, NBZ.DIR_UNTRACKED);
//				FileUtils.copyFile(new File(filePath), new File(unTrackedDir, uuid));
//				
//				fileInfo.setUntrackFileName(uuid);
//				fileInfo.setFileManageStatus(NBZ.FILE_UNTRACKED);
				
				// 如果有同名文件存在，则不往数据库中保存数据，当文件名不重复的时候就自动保存，这是一种延迟保存的处理策略。
			}else{
				// 将文件移到受管的文件夹下
				FileUtils.copyFileToDirectory(new File(filePath), managedDir);
				fileInfo.setFileManageStatus(NBZ.FILE_MANAGED);
				fileService.add(fileInfo);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// 文本发生变化的监听事件放在第一次保存文件之后
		cbCategory.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				Combo cb = (Combo) e.getSource();
				String selected = cb.getText();
				if(selected.equals(NBZ.UNDEFINED_VALUE)){
					lblNameExample.setText("");
					fileInfo.setCategory(NBZ.UNDEFINED_KEY);
				}else{
					// 显示文档名称规范示例
					lblNameExample.setText("");
					for(CategoryInfo c : categories){
						if(c.getName().equals(selected)){
							lblNameExample.setText(c.getFileNameTemplate());
							fileInfo.setCategory(c.getId());
							break;
						}
					}
				}
				onDataChanged();
			}
		});
		
		txtFileName.addFocusListener(new FocusListener() {
			
			@Override
			public void focusGained(FocusEvent e) {
				txtFileName.setData("oldData", txtFileName.getText().trim());
			}
			
			@Override
			public void focusLost(FocusEvent e) {
				if(fileNameIsUsed) 
					return;
				
				String newData = txtFileName.getText().trim();
				String oldData = (String) txtFileName.getData("oldData");
				if(!oldData.equals(newData)){
					// TODO: 
					// 1. 校验文件名是否已存在
					// 2. 修改文件的名称
					fileInfo.setFileName(newData);
					
					// TODO: 需要处理旧文件名相同，是因为文件同名的，并不是同一个文件的逻辑。
					File managedDir = NBZUtils.getManagedDir();
					if(fileInfo.getId() == 0){
						// 没有id，说明还没有托管
						try {
							new File(managedDir, newData+"."+fileInfo.getFileType()).createNewFile();
							fileService.add(fileInfo);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
					}else{
						
						File oldFile = new File(managedDir, oldData+"."+fileInfo.getFileType());
						oldFile.renameTo(new File(managedDir, newData+"."+fileInfo.getFileType()));
						onDataChanged();
					}
					
				}
			}
			
		});
		
		// 判断文件名是否重复
		txtFileName.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if(fileNameIsUsed) 
					return;
				String newData = txtFileName.getText().trim();
				FileInfo f = fileService.findFileByName(newData);
				if(f != null && f.getId()!=f.getId()/*排除本文件*/){
					fileNameIsUsed = true;
					txtFileName.setBackground(window.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
					MessageBox m = new MessageBox(window);
					m.setMessage("文件名已被使用");
					m.open();
				}else{
					txtFileName.setBackground(null);
					fileNameIsUsed = false;
				}
				
			}
		});
		
		// focusout有个问题，就是在关闭窗口时不触发，所以再监听一次销毁事件
		txtFileName.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				String newData = txtFileName.getText();
				String oldData = (String) txtFileName.getData("oldData");
				if(oldData != null && !oldData.equals(newData)){
					fileInfo.setFileName(newData);
					// TODO: 修改文件的名称
					onDataChanged();
				}
			}
		});
		
		
		cbSender.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				Combo cb = (Combo) e.getSource();
				String selected = cb.getText();
				// 获取对应的id
				for(UserInfo u : senders){
					if(u.getDisplayName().equals(selected)){
						fileInfo.setSender(u.getId());
						break;
					}
				}
				onDataChanged();
			}
		});
	}


	
	
}
