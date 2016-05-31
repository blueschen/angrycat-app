(function(angrycat, $, kendo){"use strict"
	var core = angrycat.core,
		once = core.once,
		minusTimezoneOffset = core.minusTimezoneOffset,
		plusTimezoneOffset = core.plusTimezoneOffset;
		
	function init(opts){
		var moduleName = opts.moduleName,
			rootPath = opts.rootPath,
			moduleBaseUrl = opts.moduleBaseUrl,
			gridId = opts.gridId || "#mainGrid",
			notiId = opts.notiId || "#updateNoti",
			updateInfoWindowId = opts.updateInfoWindowId || "#updateInfoWindow",
			DEFAULT_PAGE_VALUE = opts.page || 1,
			DEFAULT_PAGESIZE_VALUE = opts.pageSize || 15,
			DEFAULT_AUTOCOMPLETE_PAGESIZE_VALUE = opts.autoCompletePageSize || 25,
			DEFAULT_FILTER_VALUE = opts.filter || null,
			DEFAULT_SORT_VALUE = opts.sort || null,
			DEFAULT_GROUP_VALUE = opts.group || null,
			ORIGINAL_DEFAULT_QUERY_OPTIONS =
			{
				filter: DEFAULT_FILTER_VALUE,
				page: DEFAULT_PAGE_VALUE,
				pageSize: DEFAULT_PAGESIZE_VALUE,
				sort: DEFAULT_SORT_VALUE,
				group: DEFAULT_GROUP_VALUE				
			},	
			DEFAULT_QUERY_OPTIONS = $.extend({}, ORIGINAL_DEFAULT_QUERY_OPTIONS),
			DEFAULT_EDIT_MODE = opts.editMode || "incell",
			pk = opts.pk || "id",
			lastKendoData = opts.lastKendoData || null,
			lastSelectedCondition = opts.lastSelectedCondition || null,
			defaultDropdownItems = null,
			defaultDropdownItems3 = null,
			defaultDropdownOptionId = "select",
			selectedVal = null,
			addRowInit = opts.addRowInit,
			lockedFlag = opts.lockedFlag ? opts.lockedFlag : false;
			
		function minusFilterDateTimezoneOffset(filter, modelFields){
			if(!filter){
				return;
			}
			if(filter.filters){
				for(var i = 0; i < filter.filters.length; i++){
					var f = filter.filters[i];
					minusFilterDateTimezoneOffset(f, modelFields);
				}
			}
			if(filter.field && "date" == modelFields[filter.field].type && filter.value && (filter.value instanceof Date)){
				filter.value = minusTimezoneOffset(filter.value);
			}
		}
				
		function parseFilterDates(filter, fields){
			if(filter.filters){
				for(var i = 0; i < filter.filters.length; i++){
					parseFilterDates(filter.filters[i], fields);
				}
			}else{
				if(fields[filter.field].type == "date"){
					filter.value = plusTimezoneOffset(kendo.parseDate(filter.value));
				}
			}
		}	
		function changeFilterToMulti(filterObj, fields){
			if(!filterObj.logic && !filterObj.filters){
				return;
			}
			filterObj.logic = "or";
			var filters = filterObj.filters,
				oriFilter = filters[0];
			for(var i = 0; i < fields.length; i++){
				var field = fields[i];
				if(field !== oriFilter.field){
					filters.push($.extend({}, oriFilter, {field: field}));
				}
			}
			return filterObj;
		}
		function addSortsAsc(fields){
			var sortArray = [];
			for(var i = 0; i < fields.length; i++){
				var field = fields[i];
				sortArray.push({field: field, dir: "asc"});
			}
			return sortArray;
		}
		function getModelDataItem(ele){
			row = ele.closest("tr"),
			grid = row.closest("[data-role=grid]").data("kendoGrid"),
			dataItem = grid.dataItem(row);
			return dataItem;
		}		
		/* *********************************ui widget settings related******************************************** */
		function getAutoCompleteDefaultTemplate(fields){
			var items = 
				$.map(fields, function(element, idx){
					return "#:" + element + "#";
				});
			var result = items.join("|");
			result = ("<span>" + result + "</span>");
			return result;
		}	
		
		function getDefaultFieldAutoCompleteValidation(settings){
			var field = settings.field,
				method = settings.method,
				validate = settings.validate,
				msg = settings.msg,
				required = settings.required;
			
			return function(input){
				if(input.length == 0// 更動某個欄位，如果會一併動到其他欄位，會造成意外的檢核，所以要先判斷是否有抓到正確待檢核的欄位。
				|| !input.is("[data-bind='value:"+ field +"']")){ 
					return true;
				}
				var td = input.closest("td"),
					fieldName = field,
					row = input.closest("tr"),
					grid = row.closest("[data-role='grid']").data("kendoGrid"),
					dataItem = grid.dataItem(row),
					val = dataItem.get(fieldName);
				console.log("validate...dataItem val: " + val + ", input val: " + input.val());
				td.find("div").remove();
				
				var inputVal = input.val(),
					textField = input.attr("data-text-field");
				if(!inputVal){
					if(required){
						input.attr("data-"+ method +"-msg", "該欄位為必填");
						var requiredTimer = setInterval(function(){// 在設定input上的錯誤訊息後，kendo ui不見得會即時產生錯誤訊息元素，這導致後續移動元素的動作有時成功、有時失敗，所以設定setInterval
							var div = td.find("div");
							if(div.length > 0){
								clearInterval(requiredTimer);
								div.detach().appendTo(td).show(); // 解決錯誤訊息被遮蔽的問題 ref. http://stackoverflow.com/questions/1279957/how-to-move-an-element-into-another-element
							}
						}, 10);						
						return false;
					}
					return true;
				}
				
				if(selectedVal && selectedVal[textField] === inputVal){
					selectedVal = null;
					return true;
				}
				
				if(validate({val: val})){
					input.attr("data-"+ method +"-msg", msg);
					var selectTimer = setInterval(function(){// 在設定input上的錯誤訊息後，kendo ui不見得會即時產生錯誤訊息元素，這導致後續移動元素的動作有時成功、有時失敗，所以設定setInterval
							var div = td.find("div");
							if(div.length > 0){
								clearInterval(selectTimer);
								div.detach().appendTo(td).show(); // 解決錯誤訊息被遮蔽的問題 ref. http://stackoverflow.com/questions/1279957/how-to-move-an-element-into-another-element
							}
						}, 10);
					return false;
				}
					
				return true;
			};
		}
		
		function getDefaultFieldAutoCompleteDataSource(settings){
			var action = settings.action,
				autocompleteFieldsToFilter = settings.autocompleteFieldsToFilter,
				ds = {
				serverPaging: true,
				serverFiltering: true,
				pageSize: DEFAULT_AUTOCOMPLETE_PAGESIZE_VALUE,
				transport: {
					read:{
						url: moduleBaseUrl + "/" + action + ".json",
						type: "POST",
						dataType: "json",
						contentType: "application/json;charset=utf-8",
						cache: false	
					},
					parameterMap: function(data, type){
						if(type === "read"){
							//console.log("data: " + JSON.stringify(data));
							var r = {
								conds: {
									kendoData: {
										page: 1,
										pageSize: data.pageSize? data.pageSize : DEFAULT_AUTOCOMPLETE_PAGESIZE_VALUE,
										filter: changeFilterToMulti(data.filter, autocompleteFieldsToFilter), // autocomplete元件只有支援單一filter條件，這裡可以將他轉為多個filter條件
										sort: addSortsAsc(autocompleteFieldsToFilter)
									}
								}
							};
							return JSON.stringify(r);
						}
					}
				},
				schema:{
					type: "json",
					data: function(response){
						var results = response.results; // read
						return results;
					}
				}
			}
			return ds;
		}
		
		function getAutoCompleteCellEditor(settings){
			var textField = settings.textField,
				filter = settings.filter ? settings.filter : "contains",
				autocompleteFieldsToFilter = settings.autocompleteFieldsToFilter,
				template = settings.template ? settings.template : getAutoCompleteDefaultTemplate(autocompleteFieldsToFilter),
				errorMsgFieldName = settings.errorMsgFieldName,
				selectAction = settings.selectAction;
			return function(container, options){
				var model = options.model,
					field = options.field;
				$('<input data-text-field="'+ textField +'" data-bind="value:'+ field +'"/>')
					.appendTo(container)
					.kendoAutoComplete({
						minLength: 1,
						filter: filter,
						template: template,
						// autoBind: false, // 如果加上這行，會出現e._preselect is not a function錯誤訊息，根據官方說法，這是因為autocomplete沒有支援deferred binding
						valuePrimitive: false, // 如果選定的值，要對應物件，valuePrimitive應設為false，否則選了值之後，他會顯示[object Object]
						/*
						height: 520,
						virtual: {
							itemHeight: 26,
							valueMapper: function(options){
								console.log("valueMapper options.value: " + JSON.stringify(options.value));
								return options.value;
							}
						},*/
						dataSource: getDefaultFieldAutoCompleteDataSource(settings),
						select: function(e){
							var item = e.item,
								text = item.text(), // text is template result
								dataItem = this.dataItem(item.index());
							// ref. http://www.telerik.com/forums/autocomplete-update-grid-datasource
							// 這裡要自行綁定model值，因為如果grid該欄位有設定檢核，而且有檢核未過的記錄，第二次以後在autocomplete選到的值都無法正常加到model上
							// model.set(field, dataItem);
							selectedVal = dataItem;
							if(selectAction && (typeof selectAction === "function")){// 如果有更動其他欄位，會用原來的檢核
								selectAction(model, dataItem);
							}
						}
					});
			};
		}
		
		function getParameterDropDownEditor(settings){
			var dataTextField = settings.dataTextField,
				dataValueField = settings.dataValueField,
				data = settings.data,
				selectAction = settings.selectAction;
			return function(container, options){
				var select = '<input data-text-field="'+dataTextField+'" data-value-field="'+ dataValueField +'" data-bind="value:'+ options.field +'"/>',
					field = options.field,
					model = options.model;
				$(select)
					.appendTo(container)
					.kendoDropDownList({
						dataSource: {
							data: data
						},
						valuePrimitive: true,
						select: function(e){
							var item = e.item,
								dataItem = this.dataItem(item.index());
							if(selectAction && (typeof selectAction === "function")){
								selectAction(model, dataItem);
							}
						}
					});
			};
		}		
		
		function getParameterDropDownEditors(params, settings){
			var results = {};
			for(var prop in params){
				if(params.hasOwnProperty(prop)){
					var base = {dataTextField: "nameDefault", dataValueField: "nameDefault", data: params[prop]};
					if(settings[prop]){
						base = $.extend(base, settings[prop]);
					}
					results[prop] = getParameterDropDownEditor(base);
				}
			}
			return results;
		}

		function getParameterAutoCompleteFilterEditor(settings){
			var ele = settings.ele,
				filter = settings.filter,
				dataTextField = settings.dataTextField,
				dataValueField = settings.dataValueField,
				data = settings.data;			
			ele.kendoAutoComplete({
				valuePrimitive: true,
				dataSource: data,
				dataTextField: dataTextField,
				dataValueField: dataValueField,
				filter: filter
			});
		}
		
		function getParameterFilterEditorColumns(params, settings){
			var results = {},
				filter = settings.filter;
			for(var prop in params){
				if(params.hasOwnProperty(prop)){
					results[prop] = {
						filterable: {
							cell: {
								inputWidth: "100%",
								template: (function(data){
									return function(args){
										getParameterAutoCompleteFilterEditor({
											ele: args.element,
											dataTextField: "nameDefault",
											dataValueField: "nameDefault",
											filter: filter,
											data: data
										});
									}
								})(params[prop])
							}
						}	
					};
				}
			}
			return results;			
		}
		
		function getDefaultAutoCompleteFilterEditor(settings){
			var ele = settings.ele,
				action = settings.action,
				filter = settings.filter,
				dataTextField = settings.dataTextField,
				dataValueField = settings.dataValueField,
				ds = new kendo.data.DataSource(getDefaultFieldAutoCompleteDataSource(settings));			
			ele.kendoAutoComplete({
				valuePrimitive: true,
				dataSource: ds,
				dataTextField: dataTextField,
				filter: filter,
				dataValueField: dataValueField				
			});
		}		
		
		function getDefaultRemoteDropDownList(settings){
			var ele = settings.ele,
				readAction = settings.readAction,
				destroyAction = settings.destroyAction,
				dataTextField = settings.dataTextField,
				dataValueField = settings.dataValueField,
				valuePrimitive = settings.valuePrimitive ? settings.valuePrimitive : true,
				selectAction = settings.selectAction,
				changeAction = settings.changeAction,
				modelFields = settings.modelFields,
				autoBind = settings.autoBind ? settings.autoBind : false,
				optionLabel = settings.optionLabel ? settings.optionLabel : "請選擇條件",
				value = settings.value ? settings.value : "",
				ds = new kendo.data.DataSource({
					transport: {
						read: getDefaultRemoteConfig(readAction),
						destroy: (destroyAction ? getDefaultRemoteConfig(destroyAction) : null),
						parameterMap: function(data, type){
							if(type === "read"){
								//console.log("data: " + JSON.stringify(data));
								if(data.filter && data.filter.filters && modelFields){
									minusFilterDateTimezoneOffset(data.filter, modelFields);
								}								
								var r = {
									conds: {
										kendoData: {
											filter: data.filter
										}
									}
								};
								return JSON.stringify(r);
							}else if(type == "destroy"){
								if(data && data.id){
									return JSON.stringify(data);
								}else{
									console.log("刪除資料格式不正確");
								}
							}
						}						
					},
					schema:{
						type: "json",
						data: function(response){
							var results = response.results ? response.results : response; // read
							defaultDropdownItems = results;
							if($.isArray(results)){
								defaultDropdownItems3 = {};
								for(var i = 0; i < results.length; i++){
									var result = results[i];
									defaultDropdownItems3[result.id] = {id: result.id, name: result.name, json: JSON.stringify(result.json)};
								}
							}
							return results;
						},
						model: {
							id: pk // 加上這段設定，dataSource.sync才會驅動
						}
					},
					requestEnd: function(e){
						var type = e.type,
							response = e.response;
						if("destroy" === type){
							$(notiId).data("kendoNotification").show(response.name + "刪除成功");
						}
					}
				});
			
			var dropdown = ele.kendoDropDownList({
				valuePrimitive: valuePrimitive,
				dataSource: ds,
				dataTextField: dataTextField,
				dataValueField: dataValueField,
				select: selectAction,
				change: changeAction,
				autoBind: autoBind,
				optionLabel: optionLabel,
				value: value
			}).data("kendoDropDownList");
			return dropdown;
		}		
		
		function initDefaultNotification(options){
			var centerOnShow = function(e){
					if(e.sender.getNotifications().length == 1){
						var element = e.element.parent(),
							eWidth = element.width(),
							eHeight = element.height(),
							wWidth = $(window).width(),
							wHeight = $(window).height(),
							newTop, newLeft;
						
						newLeft = Math.floor(wWidth / 2 - eWidth / 2);
						newTop = Math.floor(wHeight / 2 - eHeight / 2);
						
						e.element.parent().css({top: newTop, left: newLeft});
					}
				},
				notiId = options.notiId,
				onShow = options.onShow ? options.onShow : centerOnShow;
			$(notiId).kendoNotification({
				stacking: "default",
				width: 300,
				height: 50,
				position: {
					pinned: false,
					bottom: 20,
					right: 20
				}
			});
		}	

		function initDefaultInfoWindow(options){
			var windowId = options.windowId,
				title = options.title;
			$(windowId).kendoWindow({
				width: "600px",
				title: title,
				visible: false,
				modal: true,
				action: [
					"Close" // other options: "Pin", "Minimize", "Maximize"
				],
				animation:{
					close:{
						effects: "fade:out",
						duration: 2000
					}
				},
				open: function(e){
					var win = this;
					setTimeout(function(){
						win.close();
					},
					60000);
				}
			});
		}		
		
		function getDefaultModelFields(fields){
			var modelFields = {};
			for(var i = 0; i < fields.length; i++){
				var field = fields[i],
					fieldName = field[0],
					type = field[3],
					customOpt = field[5];
				modelFields[fieldName] = {
					defaultValue: null,
					type: type
				};
				if(fieldName === pk){
					modelFields[fieldName]["editable"] = false;
				}
				if(customOpt){
					$.extend(modelFields[fieldName], customOpt);
				}
			}
			return modelFields;
		}
		
		function getDefaultColumns(fields){
			var columns = [],
				defaultTemplate = "<span title='#=({field} ? {field} : '')#'>#=({field} ? {field} : '')#</span>",
				defaultFilterTemplate = function(args){// 透過重新定義filter輸入欄位的template，可以阻止onchange事件每次都觸發發送request的預設行為；這樣就剩下onblur才會真的觸發查詢請求
					var parent = '<span tabindex="-1" role="presentation" style="" class="k-widget k-autocomplete k-header k-state-default"></span>';
					args.element
						.css("width", "90%") // 輸入欄位隨欄位寬度變動
						.addClass("k-input") // 讓kendo ui元件認出這是輸入欄位
						.attr("type", "text") // 讓版型更為一致
						.wrap(parent); // 跟原來預設的版型一樣，有圓角，而且與相鄰元件(按鈕)對齊
				};			
			if("incell" !== DEFAULT_EDIT_MODE){
				var idx = columns.length - 1;
				columns[idx].command.push("edit");
				columns[idx].width = "170px";
			}				
			for(var i = 0; i < fields.length; i++){
				var field = fields[i],
					fieldName = field[0],
					width = field[2],
					editor = field[7];
				var column = {
						field: fieldName,
						width: width+"px",
						title: field[1],
						filterable: {
							cell: {
								operator: field[4],
								template: "date" === field[3] ? null : defaultFilterTemplate // 如果是日期欄位，不用改filter cell template，其他要自訂預設template，這是為了防止內建change事件觸發查詢的動作
							}
						},
						template: defaultTemplate.replace(/{field}/g, fieldName)
					};
				if(editor){
					column["editor"] = editor;
				}
				if("date" === field[3]){
					var format = "yyyy-MM-dd";
					column["format"] = "{0:"+format+"}";
					column["parseFormats"] = "{0:"+format+"}";
					column["filterable"]["ui"] = function(element){
						return element.kendoDatePicker({
							format: format,
							parseFormats: [format]
						});
					};
					column["template"] = "<span>#= kendo.toString("+ fieldName +", \""+format+"\") ? kendo.toString("+ fieldName +", \""+format+"\") : \"\"#</span>"; // 如果null顯示空字串
				}
				if(field[6]){
					$.extend(column, field[6]);
				}
				columns.push(column);
			}
			columns.push({
				command: [{name: "destroy", text: "刪除"}],
				width: "75px"
			});				
			return columns;
		}

		function getDefaultRemoteConfig(action){
			return {
				url: (moduleBaseUrl + "/" + action + ".json"),
				type: "POST",
				dataType: "json",
				contentType: "application/json;charset=utf-8",
				cache: false
			};
		}

		function getDefaultGridDataSource(options){
			var modelFields = options.modelFields,
				viewModel = options.viewModel; // not required		
			return {
				batch: true,
				serverPaging: true,
				pageSize: DEFAULT_PAGESIZE_VALUE,
				page: DEFAULT_PAGE_VALUE,
				serverSorting: true,
				serverFiltering: true,
				transport: {
					create: getDefaultRemoteConfig("batchSaveOrMerge"),
					read: getDefaultRemoteConfig("queryConditional"),
					update: getDefaultRemoteConfig("batchSaveOrMerge"),
					destroy: getDefaultRemoteConfig("deleteByIds"),
					parameterMap: function(data, type){
						console.log("parameterMap type: " + type);
						//console.log("parameterMap data: " + JSON.stringify(data));
						if(type === "read"){
							if(data.filter && data.filter.filters){
								minusFilterDateTimezoneOffset(data.filter, modelFields);
							}
							var viewModelConds = viewModel ? viewModel.get("conds"): {},
								conds = $.extend({moduleName: moduleName}, viewModelConds, {kendoData: data});
							if(data.selectedCondition){
								conds["selectedCondition"] = data.selectedCondition;
							}
							return JSON.stringify({conds: conds});
						}else if(type == "create" || type == "update"){
							var dataModels = data['models'];
							if(dataModels){// batch create enabled
								return JSON.stringify(dataModels);
							}
						}else if(type == "destroy"){
							var dataModels = data['models'];
							if(dataModels){
								var ids = $.map(dataModels, function(element, idx){
									return element.id;
								});
								return JSON.stringify(ids);
							}
						}
					}
				},
				schema: {
					type: "json",
					data: function(response){
						// response.results from reading
						// response from adding or updating
						/* 
						不管任何遠端存取的動作，都需要回傳所操作的資料集，包含刪除(destroy)。
						文件上雖然說刪除不需要回傳資料，實際上如果在刪除時沒有回傳被刪除的資料，
						會讓client端的grid元件以為資料沒有commit，所以如果再次saveChange的時候，
						他會再送一次請求，而且cancel還可以恢復原狀，理論上這些都不應該發生。
						如果又加上batchUpdate的狀態下，混用新增或修改資料的動作，一次更新，
						重複saveChange，會造成重複的新增資料，但這不會在畫面上正常顯示，直到下一次更新頁面才看得出來。
						根據這篇http://www.telerik.com/forums/delete-save-changes-revert
						解決這些問題的根本做法，就是提供和讀取資料一樣的格式。
						目前已由後端提供被刪的資料。
						*/
						var results = response.results ? response.results: response;
						return results;
					},
					total: function(response){
						return response.pageNavigator.totalCount;
					},
					model: {
						id: pk,
						fields: modelFields
					}
				},
				error: function(e){
					var status = (e && e.xhr && e.xhr.status) ? e.xhr.status : null;
					if(status == 401){
						window.location.href = rootPath + "/login.jsp";
						return;
					}
					if(status != 200){
						$(updateInfoWindowId).data("kendoWindow")
							.content("<h3 style='color:red;'>主機發生錯誤</h3><br><h4><xmp>"+ JSON.stringify(e) +"</xmp></h4>")
							.center()
							.open();
					}
				},
				requestStart: function(e){
					kendo.ui.progress($(gridId).data("kendoGrid").wrapper, true);
				},
				requestEnd: function(e){
					// e.response comes from dataSource.schema.data, that is not really returned response
					var grid = $(gridId).data("kendoGrid"),
						messages = grid.options.messages.commands,
						type = e.type,
						action = messages[type];
					/*
					if("update" === type){
						$(updateInfoWindowId).data("kendoWindow").content("<h3 style='color:red;'>更新成功</h3>").center().open();
					}*/
					if(action){
						$(notiId).data("kendoNotification").show(action + "成功");	
					}
					kendo.ui.progress(grid.wrapper, false);
				}
			};
		}

		function fieldsReady(callback){
			var context = this;
			$(document).ready(function(){
				var fields = callback.call(context),
					modelFields = getDefaultModelFields(fields),
					columns = getDefaultColumns(fields),
					dataSource = getDefaultGridDataSource({modelFields: modelFields}),
					toolbar = [
					{
						text: "新增",
						name: "new",
						iconClass: "k-font-icon k-i-plus"
					},
					{
						text: "Reset",
						name: "reset",
						iconClass: "k-font-icon k-i-undo-large"
					}];
				
				if("incell" === DEFAULT_EDIT_MODE){// in relation with batch update
					toolbar.push({text: "存檔", name: "save"});
					toolbar.push({text: "回復", name: "cancel"});
				}
				toolbar.push({
					text: " 儲存條件",
					name: "saveCondition",
					iconClass: "k-font-icon k-i-lock"
				});
				toolbar.push({
					text: " 移除條件",
					name: "removeCondition",
					iconClass: "k-font-icon k-i-cut"
				});
				toolbar.push({
					text: " 選擇條件",
					name: "selectCondition"
				});
				toolbar.push({
					text: " 下載Excel",
					name: "downloadExcel",
					iconClass: "k-font-icon k-i-xls"
				});
				
				// ref. http://stackoverflow.com/questions/21112330/how-can-i-have-row-number-in-kendo-ui-grid
				var rowNumClass = "row-number";
				columns.splice(0, 1, {
					title: "&nbsp;",
					template: "<span class='"+rowNumClass+"' style='text-align: center'></span>",
					width: 50,
					locked: lockedFlag
				});
				
				var mainGrid = $(gridId).kendoGrid({
					columns: columns,
					dataSource: dataSource,
					autoBind: false,
					toolbar: toolbar,
					editable: {
						create: true, 
						update: true, 
						destroy: true,
						mode: DEFAULT_EDIT_MODE
					},
					scrollable: true,
					pageable: {
						refresh: true,
						pageSizes: ["5","10","15","20","25","30","all"],
						buttonCount: 13
					},
					sortable: {
						mode: "single",
						allowUnsort: false
					},
					navigatable: true,
					filterable: {
						mode: "menu, row",
						extra: true
					},
					selectable: "multiple, cell",
					columnMenu: true,
					resizable: true,
					dataBound: function(){
						var rows = mainGrid.items(),
							$rows = $(rows),
							ds = mainGrid.dataSource,
							ps = ds.pageSize(),
							p = ds.page(),
							count = ps * (p-1),
							highlightClass = "k-state-hover";
						// ref. http://stackoverflow.com/questions/35902267/jquery-need-help-selecting-tr-elements-inside-a-div-element-with-a-specific-clas/35917952#35917952
						$rows.hover(
							function(){
								var i = $(this).index()+1;
								$rows.filter(":nth-child(" + i + ")").addClass(highlightClass);
							},
							function(){
								var i = $(this).index()+1;
								$rows.filter(":nth-child(" + i + ")").removeClass(highlightClass);
							}
						).each(function(){
							var $row = $(this),
								index = $row.index() + 1 + count;
							$row.find("."+rowNumClass+"").html(index);
						});
					}
				}).data("kendoGrid");
				
				$(".k-grid-new").click(function(e){
					mainGrid.addRow();
					var editRow = $(".k-grid-edit-row"),
						dataItem = mainGrid.dataItem(editRow);
					if(addRowInit && (typeof addRowInit === "function")){
						addRowInit(dataItem, editRow);
					}
				});
				
				$(".k-grid-downloadExcel").click(function(e){
					var ds = mainGrid.dataSource,
						readUrl = ds.transport.options.read.url;
					window.location.href = moduleBaseUrl + "/downloadExcel.json";
				});
								
				var $selectCondition = $(".k-grid-selectCondition"),
					$input = $("<input class='k-grid-selectCondition'/>");
				$selectCondition.replaceWith($input);
				var dropdown = getDefaultRemoteDropDownList({
					ele: $input,
					readAction: "listConditionConfigs",
					destroyAction: "deleteConditionConfig",
					dataValueField: "id",
					dataTextField: "name",
					valuePrimitive: false,
					changeAction: function(){
						var dataItem = this.dataItem();
						// console.log("change dataItem.json: " + JSON.stringify(dataItem.json));
						var json3 = {};
						console.log("dataItem.id: " + dataItem.id);
						/*
						if(defaultDropdownItems){
							for(var i = 0; i < defaultDropdownItems.length; i++){
								var defaultDropdownItem = defaultDropdownItems[i];
								if(dataItem.id == defaultDropdownItem.id){
									console.log("defaultDropdownItem json: " + JSON.stringify(defaultDropdownItem.json));
									json3 = defaultDropdownItem.json;
									break;
								}
							}
						}*/
						json3 = defaultDropdownItems3[dataItem.id] ? JSON.parse(defaultDropdownItems3[dataItem.id].json) : ORIGINAL_DEFAULT_QUERY_OPTIONS;
						// 這邊不會也不用轉換date，因為從後端回來的date是UTC的string，所以也不會因為透過JSON.stringify()的轉換，讓date的值少掉八個小時
						mainGrid.dataSource.query($.extend({selectedCondition: dataItem.id}, json3));
					},
					modelFields: modelFields,
					autoBind: true,
					optionLabel: {
						id: defaultDropdownOptionId,
						name: "請選擇條件",
						json: ORIGINAL_DEFAULT_QUERY_OPTIONS
					},
					value: lastSelectedCondition ? lastSelectedCondition : defaultDropdownOptionId
				});
				
				$(".k-grid-removeCondition").click(function(e){
					var ddds = dropdown.dataSource,
						items = ddds.data(),
						selected = dropdown.value();
					for(var i = 0; i < items.length; i++){
						var item = items[i];
						if(selected == item.id){
							var newSelected = null;
							if(items[i+1]){ // 找看看後面選項是否存在
								newSelected = items[i+1].id;
							}else if(items[i-1]){// 找看看前面選項是否存在
								newSelected = items[i-1].id;
							}else{// 預設選項
								newSelected = defaultDropdownOptionId;
							}
							ddds.remove(item);
							ddds.sync().then(function(e){
								dropdown.value(newSelected);
								dropdown.trigger("change");
							});
							break;
						}
					}
				});
				
				$(".k-grid-reset").click(function(e){
					var optionLabel = dropdown.options.optionLabel;
					if(optionLabel && optionLabel.id == defaultDropdownOptionId){
						dropdown.value(defaultDropdownOptionId);
						dropdown.trigger("change");
					}else{
						var ds = mainGrid.dataSource;
						ds.query(DEFAULT_QUERY_OPTIONS);
					}
				});
				$(".k-grid-saveCondition").click(function(e){
					var name = prompt("請輸入設定名稱");
					if(!name){
						return;
					}
					var ds = mainGrid.dataSource,
						query = {
							page: ds.page(),
							pageSize: ds.pageSize(),
							sort: ds.sort(),
							group: ds.group(),
							filter: ds.filter()
						},
						baseConfig = getDefaultRemoteConfig("saveCondition");
										
					var moduleConfig = {
						name: name,
						moduleName: moduleName,
						json: query // 此處日期欄位不需轉換，假設我在filter填入2016-04-26，這邊出來的資料會是Tue Apr 26 2016 08:00:00 GMT+0800；透過JSON.stringify(減掉八小時)之後，並不會變更日期；有趣得是，如果是直接從dataSource取得的日期資料，會變成Tue Apr 26 2016 00:00:00 GMT+0800，JSON.stringify(減掉八小時)之後，傳出的時間就不正確
					};
					var data = JSON.stringify(moduleConfig);					
					$.ajax(baseConfig.url, $.extend(baseConfig, {// TODO 是否換成kendo ui dataSource
						data: data,
						traditional: true,
						beforeSend: function(jqxhr, settings){
							kendo.ui.progress(mainGrid.wrapper, true);
						},
						complete: function(){
							kendo.ui.progress(mainGrid.wrapper, false);
						},
						statusCode: {
							401: function(){
								window.location.href = rootPath + "/login.jsp";
								return;
							}
						},
						success: function(ret, textStatus, jqxhr){
							dropdown.dataSource.add(ret);
							dropdown.value(ret.id);
							defaultDropdownItems3 = defaultDropdownItems3 || {};
							defaultDropdownItems3[ret.id] = {id: ret.id, name: ret.name, json: JSON.stringify(ret.json)};
							$(notiId).data("kendoNotification").show("條件儲存成功");
						},
						error: function(jqxhr){
							$(updateInfoWindowId).data("kendoWindow")
								.content("<h3 style='color:red;'>主機發生錯誤</h3><br><h4><xmp>"+ JSON.stringify(jqxhr.statusText) +"</xmp></h4>")
								.center()
								.open();
						}
					}));
				});
				
				if(DEFAULT_EDIT_MODE === "incell"){
					var tdTotal = 0;
					mainGrid.tbody.on("keydown", "td[data-role='editable'] input", function(e){
						var $target = $(e.target);
						if(e.keyCode == 13){
							/* not work!!
							$(gridId).data("kendoGrid").one("afterClose", function(e){
								var $td = e.container,
									tdIdx = $td.index(),
									$tr = $td.closest("tr");
						
								do{
									var nextCell = $tr.find("td:eq("+ (++tdIdx) +")");
								}while(nextCell.css("display") === "none");
					
								if(nextCell.length === 0){
									return;
								}
								setTimeout(function(){
									var grid = $(gridId).data("kendoGrid");
									grid.current(nextCell);
									grid.editCell(nextCell);
								},0);
							});
							 */
							mainGrid.options["afterClose"] = once(function(e){ // 不得已直接從kendo ui widget的options加入事件處理器
								var $td = e.container,
									tdIdx = $td.index(),
									$tr = $td.closest("tr");
								if(!tdTotal){
									tdTotal = $tr.find("td").length;
								}
								do{
									if(tdIdx+1 >= tdTotal){// 如果已經是該行最後一欄，從下一行前面開始
										$tr = $tr.next("tr");
										tdIdx = 0;// TODO 第一列可能是或不是可編輯儲存格
									}
									var nextCell = $tr.find("td:eq("+ (++tdIdx) +")");
								}while(nextCell.css("display") === "none"); // 如果是隱藏欄位就跳下一筆
						
								if(nextCell.length === 0){
									return;
								}
								setTimeout(function(){
									var grid = $(gridId).data("kendoGrid");
									grid.current(nextCell);
									grid.editCell(nextCell);
								},0);
							});
						}
					});
				}
				
				$(document.body).keydown(function(e){
					var altKey = e.altKey,
						keyCode = e.keyCode;
					// ref. http://demos.telerik.com/kendo-ui/grid/keyboard-navigation
					if(altKey && keyCode == 87){// Alt + W 就可以跳到grid table；搭配navigatable設定，可用上下左右鍵在grid cell上移動；遇到可編輯cell，可以Enter進去編輯，編輯完畢按下Enter
						mainGrid.table.focus();
					}
					if(altKey && keyCode == 82){// Alt + R 直接觸發 Add new record
						mainGrid.addRow();
					}
					if(altKey && keyCode == 67){// Alt + C 直接觸發 Save Changes；
						mainGrid.dataSource.sync();
					}
					if(altKey && keyCode == 81){// Alt + Q 直接觸發 Cancel changes
						mainGrid.dataSource.cancelChanges();
					}
					/*
					if(altKey && keyCode == 71){// Alt + G 直接觸發 Delete；
						mainGrid.current().closest(".k-grid-delete").click();
					}
					
					if(e.altKey && e.keyCode == 69){// Alt + E 直接觸發 Edit；
						mainGrid.current().closest(".k-grid-edit");
					}
					if(e.altKey && e.keyCode == 85){// Alt + U 直接觸發 Update；
						mainGrid.current().closest(".k-grid-update").click();
					}	
					if(e.altKey && e.keyCode == 67){// Alt + C 直接觸發 Cancel；
						mainGrid.current().closest(".k-grid-cancel").click();
					}
					*/				
					if(e.ctrlKey && altKey && keyCode == 65){// Ctrl + Alt + A 執行批次複製, ref. http://stackoverflow.com/questions/24273432/kendo-ui-grid-select-single-cell-get-back-dataitem-and-prevent-specific-cells
						var grid = mainGrid,
						    selection = grid.select(); // 回傳jQuery物件，裡面可能是被選取的cells或rows
						if(!selection){
							return;
						};
					    var startColIdx = selection.index(), // 如果多選column，只會顯示最左邊的欄位index；如果單選column，就是該欄位的index；0 based, 隱藏欄位會被計算
					    	lastColIdx = selection.last().index(), // td的index, ref. http://stackoverflow.com/questions/788225/table-row-and-column-number-in-jquery
					    	columnCount = (lastColIdx - startColIdx + 1), // 橫跨的column數量
					    	selectedCount = selection.size(), // 有幾個cell被選擇
					    	rowCount = (selectedCount / columnCount), // 包含的row數量
					    	columnOpts = grid.options.columns,
					    	colFieldName = grid.options.columns[startColIdx].field, // get column field name
							firstRow = selection.closest("tr"), // 如果多選的時候，只會拿到第一個row
							firstDataItem = grid.dataItem(firstRow), // 如果多選的時候，只會拿到第一個dataItem
							fields = [];
						
					    console.log("firstDataItem: " + JSON.stringify(firstDataItem));
					    
					    for(var i = startColIdx; i < (lastColIdx+1); i++){
					    	var field = columnOpts[i].field;
					    	fields.push(field);
					    }
						// 如果多選的時候，要取得所有row的dataItem，要跑迴圈
						// 如果透過jQuery的each函式更新dataItem，會使selection的elements產生變化，以致於接下來的更新動作全部失敗。解決方式是:先取得所有dataItem，然後一次修改他們。
						var dataItems = selection.map(function(idx, cell){
							//alert($(cell).eq(0).text()); // 可直接取得cell值
							if(idx > (columnCount-1)){
								var row = $(cell).closest("tr"),
									dataItem = grid.dataItem(row);
								return dataItem;
							}
						});
						for(var i = 0; i < dataItems.length; i++){
							var dataItem = dataItems[i];
							for(var j = 0; j < fields.length; j++){
								var field = fields[j];
								dataItem.set(field, firstDataItem.get(field));
							}
						};
					}
				});
				
				initDefaultInfoWindow({windowId: updateInfoWindowId, title: "更新訊息"});
				initDefaultNotification({notiId: notiId});
				
				if(lastKendoData){// 沒有date型別轉換問題，因為從後端回來的date在前端已經是UTC的string
					mainGrid.dataSource.query(lastKendoData);
				}else{
					mainGrid.dataSource.query(DEFAULT_QUERY_OPTIONS);
				}
			});
		}
	
		return {
			getAutoCompleteDefaultTemplate: getAutoCompleteDefaultTemplate,
			getAutoCompleteCellEditor: getAutoCompleteCellEditor,
			getDefaultFieldAutoCompleteDataSource: getDefaultFieldAutoCompleteDataSource,
			getParameterDropDownEditors: getParameterDropDownEditors,
			getParameterFilterEditorColumns: getParameterFilterEditorColumns,
			initDefaultInfoWindow: initDefaultInfoWindow,
			getDefaultModelFields: getDefaultModelFields,
			getDefaultColumns: getDefaultColumns,
			getDefaultGridDataSource: getDefaultGridDataSource,
			getDefaultFieldAutoCompleteValidation: getDefaultFieldAutoCompleteValidation,
			getDefaultRemoteDropDownList: getDefaultRemoteDropDownList,
			getDefaultAutoCompleteFilterEditor: getDefaultAutoCompleteFilterEditor,
			fieldsReady: fieldsReady
		};
	}
	angrycat.kendoGridService = {
		init: init
	};
	// on closeCell function, add custom event
	kendo.ui.Grid.fn.closeCell = (function(closeCell){ // ref. http://jsfiddle.net/lhoeppner/nx96g/10/
		return function(cancel){
			var that = this,
				container = that._editContainer;				
			closeCell.call(this, cancel);
			if(!container){
				return;
			}
			var tdIdx = container.index(),
				model = that.dataSource.getByUid(container.closest("tr").attr(window.kendo.attr("uid"))),
				event = {
					container: container,
					model: model,
					preventDefault: function(){
						this.isDefaultPrevented = true;
					}
				};
			var afterCloseHandler = this.options.afterClose;
			if(model && typeof afterCloseHandler === "function"){
				afterCloseHandler.call(this, event);
				if(event.isDefaultPrevented){
					return;
				}
			}
		};
	})(kendo.ui.Grid.fn.closeCell);	
	
	
	
	
	
	
})(angrycat, jQuery, kendo);