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
			lockedFlag = opts.lockedFlag ? opts.lockedFlag : false,
			editAction = opts.editAction,
			docType = opts.docType;
			
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
			if(filter.field && modelFields[filter.field] && "date" == modelFields[filter.field].type && filter.value && (filter.value instanceof Date)){
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
				},
				error: function(e){
					var status = (e && e.xhr && e.xhr.status) ? e.xhr.status : null;
					defaultResponseHandler(status);
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
				selectAction = settings.selectAction,
				valuePrimitive = settings.valuePrimitive ? settings.valuePrimitive : false;
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
						valuePrimitive: valuePrimitive, // 如果選定的值，要對應物件，valuePrimitive應設為false，否則選了值之後，他會顯示[object Object]
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
					},
					error: function(e){
						var status = (e && e.xhr && e.xhr.status) ? e.xhr.status : null;
						defaultResponseHandler(status);
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
		function defaultResponseHandler(status){
			if(status == 401){
				window.location.href = rootPath + "/login.jsp";
				return;
			}
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
				},
				numberFilterTemplate = function(args){// 預設數字篩選會格式會多0，透過這個函式，去掉多餘的0
					args.element.kendoNumericTextBox({
						format: "n0",
						decimal: 0
					});
				},
				dateFilterTemplate = function(args){
					args.element.kendoDatePicker({
						format: "yyyy-MM-dd"
					});
				};			
			if("incell" !== DEFAULT_EDIT_MODE){
				var idx = columns.length - 1;
				columns[idx].command.push("edit");
				columns[idx].width = "170px";
			}				
			for(var i = 0; i < fields.length; i++){
				var field = fields[i],
					fieldName = field[0],
					type = field[3],
					width = field[2],
					editor = field[7],
					columnTemplate = field[8];
				columnTemplate = columnTemplate ? columnTemplate : defaultTemplate.replace(/{field}/g, fieldName);
				var column = {
						field: fieldName,
						width: width+"px",
						title: field[1],
						filterable: {
							cell: {
								operator: field[4],
								template: type === "string" ? defaultFilterTemplate : (type === "number" ? numberFilterTemplate : (type == "date" ? dateFilterTemplate : null)) // 字串型別欄位，才要更動filter cell template，這是為了防止內建change事件觸發查詢的動作
							}
						},
						template: columnTemplate
					};
				if(editor){
					column["editor"] = editor;
				}
				if("date" === type){
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
				command: [
				    {name: "destroy", text: "刪除"},
				    {name: "datachangelog", text: "異動記錄"}
				],
				width: "200px"
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
					defaultResponseHandler(status);
					if(status != 200){
						var regex = /<body><h1>.*Exception:\s(.*)<\/h1>/;
						var rs = regex.exec(e.xhr.responseText);
						var msg = e.xhr.responseText;
						if(rs && rs.length >= 2){
							var txt = document.createElement("textarea");
						    txt.innerHTML = rs[1];
							msg = txt.value;							
						}
						$(updateInfoWindowId).data("kendoWindow")
							.content("<h3 style='color:red; width: 50%;'>主機發生錯誤code:"+ status +"</h3>"+ msg)
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
		var divLocked = "div.k-grid-content-locked",
			divUnlocked = "div.k-grid-content";
		function isLocked(ele){
			return $(ele).closest(divLocked).length > 0;
		}
		function getLockCount(){
			var lockCount = $(divLocked + " > table > tbody > tr:nth-child(1) > td").length;
			return lockCount;
		}
		function getFieldViaIdx(idx, mainGrid){ // 如果有lock table，這邊傳入的idx要合併計算
			if(!mainGrid){
				mainGrid = $(gridId).data("kendoGrid");
			}
			var field = mainGrid.element.find("th[data-index="+idx+"]").attr("data-field");
			return field;
		}
		function getCellIdx(cell){
			var $cell = $(cell),
				eleIdx = $cell.index(),
				tdIdx = eleIdx;
			if(!isLocked(cell)){
				var lockCount = getLockCount();
				tdIdx += lockCount;
			}
			return tdIdx;
		}
		function getFieldViaCell(cell){
			var tdIdx = getCellIdx(cell);
			var field = getFieldViaIdx(tdIdx);
			return field;
		}
		function getColumnViaCell(cell, mainGrid){
			if(!mainGrid){
				mainGrid = $(gridId).data("kendoGrid");
			}
			var field = getFieldViaCell(cell),
				columns = mainGrid.options.columns;
			for(var i = 0; i < columns.length; i++){
				var column = columns[i];
				if(column.field === field){
					return column;
				}
			}
		}
		function fieldsReady(callback, afterGridInit){
			var context = this,
				getScrollDimensions = 
				function(){// ref. http://stackoverflow.com/questions/457262/html-what-is-the-height-of-a-horizontal-scrollbar
					// 計算水平捲軸的寬高
					var el = document.createElement("div");
					el.style.visibility = "hidden";
					el.style.overflow = "scroll";
					document.body.appendChild(el);
					var w = el.offsetWidth-el.clientWidth;
					var h = el.offsetHeight-el.clientHeight;
					document.body.removeChild(el);
					return {height: h, width: w};
				},
				dimensions = getScrollDimensions(),
				scrollbarHeight = dimensions.height ? dimensions.height : 10;
			$(document).ready(function(){
				var $doc = $(this),
					fields = callback.call(context),
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
				// 取消頁面最外圍水平和垂直捲軸
				$(document.body).css("overflow", "hidden");
				if("incell" === DEFAULT_EDIT_MODE){// in relation with batch update
					toolbar.push({text: "存檔", name: "save"});
					toolbar.push({text: "回復", name: "cancel"});
				}
				toolbar.push({
					text: "|",
					name: "divider"
				});
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
				columns.splice(0, 0, {
					title: "&nbsp;",
					template: "<span class='"+rowNumClass+"' style='text-align: center'></span>",
					width: 50,
					locked: lockedFlag
				});
				var docHeight;
				function dataChangeClkHandler(){
					var dataItem = mainGrid.dataItem($(this).closest("tr"));
					var id = dataItem[pk];
					if(id && docType){
						var url = rootPath + "/datachangelog/list?docId=" + id + "&docType=" + docType;
						window.location.href = url;
					}
				}
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
					scrollable: true, // ref. http://docs.telerik.com/kendo-ui/controls/data-management/grid/appearance#make-the-grid-100-high-and-auto-resizable
					pageable: {
						refresh: true,
						pageSizes: ["50","100","all"],
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
					height: 500, // 加上固定高度，搭配scrollable = true，就可以啟用Kendo UI管理的垂直捲軸；如果資料內容超過可視範圍，keyboard navigation上下鍵才能正常運作(需先啟用navigatable)
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
						
						// 在Google Chrome，如果啟用keyboard navigate及scrollable(設為true)功能，且當次資料列內容超過可視範圍(已出現垂直捲軸)，focus在特定content cell中，他會自動將focused cell所在資料列切齊在螢幕最上方。
						// 這會造成一個困擾:就是Kendo Grid位於內容table上方的模組，譬如標頭、篩選列、工具列、全部被擠到上方不可見範圍
						// 如果使用者接下來要參照或操作這些模組，還得往上捲
						// ref. https://blog.longle.net/2012/03/21/setting-teleriks-html5-kendoui-grids-height-to-100-when-binding-to-remote-data/
						// 可再試試 http://jsfiddle.net/dimodi/SDFsz/
						// 最新官方說明文件:(目前沒有完全按照官方說明實作，只有設定grid高度、呼叫resize方法、設定toolbar的margin-top) 
						// http://docs.telerik.com/kendo-ui/controls/data-management/grid/appearance#set-a-100-height-and-auto-resize
						// http://docs.telerik.com/kendo-ui/controls/data-management/grid/how-to/Layout/resize-grid-when-the-window-is-resized
						var newDocHeight = $doc.height();
						if(!docHeight || docHeight!=newDocHeight){
							docHeight = newDocHeight;
							var navbarHeight = $("#navbarDiv").height(); // 非Kendo UI模組的(固定)置頂瀏覽列
							var newGridHeight = docHeight - navbarHeight - scrollbarHeight * 1.4; // Grid的高度還要扣掉水平捲軸的高度才是正確的
							
							$(gridId).height(newGridHeight); // 設定grid新高度，必須多扣，讓底部多留空間，目前計算方式就是多扣scrollbar高度40%，這讓Google Chrome也可以正常運作；如果沒有，每次重拿document的高度都會多2，導致grid高度逐漸增加，目前原因不明
							mainGrid.resize(); // 透過這種校對，凍結欄位造成下方部分資料列被切掉，及兩側資料列高度不一致的情況，都可以處理
							
							$doc.find(".k-grid-toolbar").css("margin-top", navbarHeight + "px"); // 因為置頂的瀏覽列是固定的，所以margin-top要等於瀏覽列的高度，這樣瀏覽器自動調整捲軸位置的時候，才不會切到最上方區塊
							// 啟用鎖定欄位後，會產生左右兩側table。在頁面初始化過程中，兩者的高度會不同，導致被鎖定table的最後一筆資料看來好像被部分切掉。
							// 所以透過沒被鎖定的table高度校正，兩者看來才會一致
							// 使用resize就能夠處理凍結欄位兩側高度不一致問題，所以將以下處理方式註解起來
							//$("div.k-grid-content-locked").height($content.height()-scrollbarHeight);
						}
						
						$rows.find(".k-grid-datachangelog")
							.unbind("click", dataChangeClkHandler)
							.click(dataChangeClkHandler);
					},
					edit: editAction
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
				})
				.attr("data-toggle", "tooltip")
				.attr("data-placement", "top")
				.attr("title", "初始條件查詢(不包含運算子)")
				.tooltip()
				.click(function(){
					$(this).blur();//加掛tooltip之後，按下按鈕會產生焦點，這會影響grid某些行為，譬如內容區塊的焦點無法移動的問題。所以此處跳脫焦點。
				});
				
				$(".k-grid-cancel-changes")
				.attr("data-toggle", "tooltip")
				.attr("data-placement", "top")
				.attr("title", "取消頁面資料異動")
				.tooltip()
				.click(function(){
					$(this).blur();//加掛tooltip之後，按下按鈕會產生焦點，這會影響grid某些行為，譬如內容區塊的焦點無法移動的問題。所以此處跳脫焦點。
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
								defaultResponseHandler(401);
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
							var status = jqxhr.status;
							var regex = /<body><h1>.*Exception:\s(.*)<\/h1>/;
							var rs = regex.exec(jqxhr.responseText);
							var msg = jqxhr.responseText;
							if(rs && rs.length >= 2){
								var txt = document.createElement("textarea");
							    txt.innerHTML = rs[1];
								msg = txt.value;	
							}
							$(updateInfoWindowId).data("kendoWindow")
								.content("<h3 style='color:red;'>儲存條件時主機發生錯誤code:"+ status +"</h3>"+ msg)
								.center()
								.open();
						}
					}));
				});
				// 在toolbar按鈕間產生垂直分隔線
				$("a.k-grid-divider").replaceWith("<span class='v-divider'></span>");
				/*	ref. http://stackoverflow.com/questions/28828228/kendo-grid-how-to-set-focus-back-to-a-grid-cell-after-canceling-current-editing			
				if(DEFAULT_EDIT_MODE === "incell"){
					var tdTotal = 0;
					mainGrid.tbody.on("keydown", "td[data-role='editable'] input", function(e){
						var $target = $(e.target);
						if(e.keyCode == 13){
							 not work!!
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
							/*
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
				*/
				$(document.body).keydown(function(e){
					var altKey = e.altKey,
						keyCode = e.keyCode;
					// ref. http://demos.telerik.com/kendo-ui/grid/keyboard-navigation
					if(altKey && keyCode == 87){// Alt + W 就可以跳到grid table；搭配navigatable設定，可用上下左右鍵在grid cell上移動；遇到可編輯cell，可以Enter進去編輯，編輯完畢按下Enter
						mainGrid.table.focus();
					}
					if(altKey && keyCode == 82){// Alt + R 直接觸發 Add new record
						//mainGrid.addRow();
						$(".k-grid-new").click();
					}
					if(altKey && keyCode == 67){// Alt + C 直接觸發 Save Changes；
						//mainGrid.dataSource.sync();
						$(".k-grid-save-changes").click();
					}
					if(altKey && keyCode == 81){// Alt + Q 直接觸發 Cancel changes
						//mainGrid.dataSource.cancelChanges();
						$(".k-grid-cancel-changes").click();
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
						var selected = mainGrid.select();
						//console.log("selected count: " + selected.length);
						if(selected.length == 0){
							return true;
						}
						var columns = mainGrid.options.columns,
							firstDataItem = mainGrid.dataItem(selected.first().closest("tr")),
							firstTd = null;
					
						selected.each(function(idx, ele){
							var $ele = $(ele),
								field = getFieldViaCell(ele),
								dataItem = mainGrid.dataItem($ele.closest("tr"));
						
							if(idx == 0 && dataItem === firstDataItem){
								firstTd = $ele;
							}
						
							if(dataItem !== firstDataItem){
								var newVal = firstDataItem.get(field);
								for(var i = 0; i < columns.length; i++){
									var column = columns[i];
									if(column.field === field
									&& column.editable !== false
									&& JSON.stringify(newVal) != JSON.stringify(dataItem.get(field))){
										mainGrid.editCell($ele); // 每次dataItem.set()都會觸發頁面reprint，讓前次的dirty flag消失；在dataItem.set()之前呼叫editCell，可以阻止頁面reprint，保留每一個dirty flag ref. http://www.telerik.com/forums/kendo-mvc-grid-batch-mode-dataitem-set-clears-the-dirty-flags
										dataItem.set(field, newVal);
									}
								}
							}
						});
					
						mainGrid.closeCell();
						mainGrid.clearSelection();
						firstTd.closest("table").focus(); //ref. http://stackoverflow.com/questions/28828228/kendo-grid-how-to-set-focus-back-to-a-grid-cell-after-canceling-current-editing
					}
				});
				
				initDefaultInfoWindow({windowId: updateInfoWindowId, title: "更新訊息"});
				initDefaultNotification({notiId: notiId});
				
				if(lastKendoData){// 沒有date型別轉換問題，因為從後端回來的date在前端已經是UTC的string
					mainGrid.dataSource.query(lastKendoData);
				}else{
					mainGrid.dataSource.query(DEFAULT_QUERY_OPTIONS);
				}
				
				if(typeof afterGridInit === "function"){
					afterGridInit(mainGrid);
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
			fieldsReady: fieldsReady,
			getLockCount: getLockCount,
			getFieldViaIdx: getFieldViaIdx,
			getColumnViaCell: getColumnViaCell,
			getFieldViaCell: getFieldViaCell
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