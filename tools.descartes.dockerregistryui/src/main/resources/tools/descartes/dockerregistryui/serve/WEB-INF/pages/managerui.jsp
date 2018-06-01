<%@ page session="false"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><!DOCTYPE html>
<html>
<head>
	<title>Docker Registry</title>
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
	<link rel="icon" type="image/png" href="<c:url value='/favicon.png'/>">
</head>
<body>
	<div class="container-fluid">
		<div class="row">
  			<div class="col-xs-12">
  				<h1>Docker Registry</h1>
  				<h4>@ ${registryHost}</h4>
  				<div>
  					${hello}
  				</div>
  			</div>
		</div>
		
		<div id="images" class="row"><div class="col-xs-12">
			<h2>Available Images</h2>
			<div class="btn-toolbar" role="toolbar" aria-label="Kategorie Toolbar">
			  <div class="btn-group" role="group" aria-label="Alle Kategorien">
			    <form action="./#images" accept-charset="UTF-8">
					<button type="submit" class="btn btn-default"><span class="glyphicon glyphicon-plus"></span> All</button>
				</form>
			  </div>
			  <c:forEach items="${categories}" var="category">
			    <div class="btn-group" role="group" aria-label="${category.name} Button Group">
					<form action="./#images" accept-charset="UTF-8">
						<input type="hidden" name="category" value="${category.id}"/>
						<button type="submit" class="btn btn-primary" style="background-color: ${category.color};"><span class="glyphicon glyphicon-menu-down"></span> ${category.name}</button>
					</form>
				</div>
			</c:forEach>
			  <div class="btn-group" role="group" aria-label="Edit Categories Group">
			    <button type="button" class="btn btn-default" data-toggle="modal" data-target="#categoryeditmodal"><span class="glyphicon glyphicon-pencil"></span> Edit</button>
			  </div>
			</div>
			<table class="table table-striped">
			<thead><th></th><th>Image</th><th>Tags</th><th>Description</th><th>Example Run</th><th></th></thead>
				<tbody>
					<c:forEach items="${images}" var="image"><tr>
						<td>
							<div class="btn-group-vertical btn-group-xs" role="group">
								<c:forEach items="${image.description.categories}" var="category">
									<span class="btn btn-primary" style="background-color: ${category.color}; cursor: default;">${category.name}</span>
								</c:forEach>
							</div>
						</td>
						<td><b>${image.imageName}</b></td>
						<td>${image.formattedTags}</td>
						<td><c:if test="${not empty image.description}">${image.formattedDescription}</c:if></td>
						<td class="col-md-6"><c:if test="${not empty image.description}"><code>${image.formattedExampleCommand}</code></c:if></td>
						<td><button type="button" class="btn btn-default btn-sm" data-toggle="modal" data-target="#${image.imageName}modal"><span class="glyphicon glyphicon-pencil"></span></button></td>
					</tr></c:forEach>	
				</tbody>
			</table>
			<%-- Image description edit modals. --%>
			<c:forEach items="${images}" var="image">
				<div id="${image.imageName}modal" class="modal fade" role="dialog">
					<div class="modal-dialog">
						<div class="modal-content">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal">&times;</button>
								<h4 class="modal-title">${image.imageName}</h4>
							</div>
							<c:if test="${not empty image.description}">
								<div class="modal-body">
									<div class="form-group">
										<label for="${image.imageName}categorylist">Categories of ${image.imageName}</label>
										<div id="${image.imageName}categorylist">
										<c:forEach items="${image.description.categories}" var="category">
											<div class="btn-group" role="group">
												<form method="post" action="./removecategoryfromimage" accept-charset="UTF-8">
													<input type="hidden" name="category" value="${category.id}"/>
													<input type="hidden" name="image" value="${image.description.id}"/>
													<button type="submit" class="btn btn-primary" style="background-color: ${category.color};">
														${category.name} <span class="glyphicon glyphicon-remove-circle"></span>
													</button>
												</form>
											</div>
										</c:forEach>
										</div>
									</div>
									<div class="form-group">
										<label for="${image.imageName}categorylist">Other Categories</label>
										<div id="${image.imageName}categorylist"> <c:forEach items="${categories}" var="category">
											<c:if test="${not image.description.categories.contains(category)}">
												<div class="btn-group" role="group">
													<form method="post" action="./addcategorytoimage" accept-charset="UTF-8">
														<input type="hidden" name="category" value="${category.id}"/>
														<input type="hidden" name="image" value="${image.description.id}"/>
														<button type="submit" class="btn btn-primary" style="background-color: ${category.color};">
															<span class="glyphicon glyphicon-plus"></span> ${category.name}
														</button>
													</form>
												</div>
											</c:if>
										</c:forEach></div>
									</div>
								</div>
							</c:if>
							<form method="post" action="./imagedescription" accept-charset="UTF-8">
								<div class="modal-body">
									<div class="form-group">
										<label for="${image.imageName}description">Short Description</label>
										<textarea name="description" class="form-control" rows="4" id="${image.imageName}description" placeholder="Short Description"><c:if test="${not empty image.description}">${image.description.description}</c:if></textarea>
										<small class="form-text text-muted">Supports <a href="http://commonmark.org/help/">CommonMark Markdown</a> formatting.</small>
									</div>
									<div class="form-group">
										<label for="${image.imageName}exampleCommand">Example Docker-Run</label>
										<input type="text" name="exampleCommand" class="form-control" id="${image.imageName}exampleCommand" placeholder="Example Docker run command"<c:if test="${not empty image.description}"> value="${image.description.exampleCommand}"</c:if>/>
										<input type="hidden" name="imageName" value="${image.imageName}"/>
									</div>
								</div>
								<div class="modal-footer">
									<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
									<button type="submit" class="btn btn-primary">Update</button>
								</div>
							</form>
						</div>
					</div>
			</div>
			</c:forEach>	
			<%-- Category Edit Modal --%>
				<div id="categoryeditmodal" class="modal fade" role="dialog">
					<div class="modal-dialog"><div class="modal-content">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal">&times;</button>
								<h4 class="modal-title">Categories</h4>
							</div>
							<div class="modal-body">
								<c:forEach items="${categories}" var="category">
									<div class="btn-group" role="group" aria-label="${category.name} Button Group">
										<form method="post" action="./removecategory" accept-charset="UTF-8">
											<input type="hidden" name="id" value="${category.id}"/>
											<button type="submit" class="btn btn-primary" style="background-color: ${category.color};">
												${category.name} <span class="glyphicon glyphicon-remove-circle"></span>
											</button>
										</form>
									</div>
								</c:forEach>
							</div>
							<div class="modal-body">
								<form class="form-inline" method="post" action="./createcategory" accept-charset="UTF-8">
									<div class="form-group">
										<label for="newCategoryName">New Category</label>
										<input type="text" name="name" class="form-control" id="newCategoryName" placeholder="Name"/>
									</div>
									<input type="color" name="color" value="#42adf4"/>
									<button type="submit" class="btn btn-primary">Create</button>
								</form>
							</div>
							<div class="modal-footer">
								<button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
							</div>
				</div></div>
			</div>
		</div></div>
	</div>
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
</body>
</html>
