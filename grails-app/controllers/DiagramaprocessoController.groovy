import java.util.Date;  
import java.util.Calendar;  
import java.text.SimpleDateFormat;
import java.text.DateFormat;
class DiagramaprocessoController {
    
    def index = { redirect(action:list,params:params) }

    // 
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
	
	def upload={
	    def NSA='';def nomeProjeto='';def nomeUsuario='';def dataCadastro=''
	    if(request.method=='GET'){
			session["NSA"]=Projeto.get(params.id).id
			nomeProjeto=Projeto.get(params.id).nomeProjeto
			nomeUsuario=Projeto.get(params.id).usuario
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			dataCadastro=df.format(Projeto.get(params.id).dataCadastro);
			return [ nomeProjeto : nomeProjeto,nomeUsuario:nomeUsuario,dataCadastro:dataCadastro ]
			 
		}
		if(request.method=='POST'){
		def file = request.getFile('file')	
		if(file.empty) {
			flash.message = "Não foi possível tranferir o arquivo!!!"} 
		else{
		def documentInstance = new Diagramaprocesso()
		documentInstance.fileName = file.originalFilename
		documentInstance.type = file.contentType
		documentInstance.dataCadastro=new Date()
		documentInstance.dataEdicao=new Date()
		documentInstance.projeto=session["NSA"]
		documentInstance.fileData=file.getBytes()
		documentInstance.fullPath = grailsApplication.config.uploadFolder.toString()+file.originalFilename.toString()
		
		//Tranferindo arquivo dinâmicamente

		def nomeOriginal = file.originalFilename
		documentInstance.fileName = nomeOriginal
		if(!file.empty){
			file.transferTo(new File("web-app/arquivos/paulo/${nomeOriginal}"))
		}else{
		    flash.message = "Não foi possível tranferir o arquivo!!!" 
		}
		
		if(file.contentType.toString().equals("application/octet-stream"))
		{
			  flash.message = "Vazio, nao pode ser carregado!!!"
			  redirect (action:'list')
		}

		if(file.contentType.toString().equals("text/xml"))
		{
			  file.transferTo(new File(documentInstance.fullPath))
			  documentInstance.save()
			  flash.message = "Arquivo salvo com sucesso!!! ${session["NSA"]}"
			  redirect(action:show,id:documentInstance.id)
		}
		}
		if(file.contentType.toString()!="text/xml")
		{
			  flash.message = "Arquivo não pode ser carregado, extensão de arquivo errada *bpmn: ${file.contentType}"
			  redirect (action:'list')
		}
		
	  }// IF Post
	}
	
	def download={ 
		Diagramaprocesso diagramaprocesso = Diagramaprocesso.get(params.id)
		if(diagramaprocesso == null)
		{
		  flash.message = "Arquivo não encontrado!!!"
		  redirect(action:list)
		}
		else{
		  response.setContentType("APPLICATION/OCTET-STREAM")	
		  response.setHeader("Content-Disposition","Attachment;Filename=\"${diagramaprocesso.fileName}\"")
		  
		  def outputStream = response.getOutputStream()
		      outputStream << diagramaprocesso.fileData
			  outputStream.flush()
			  outputStream.close()
		}
		
	}

    def list = {
        if(!params.max) params.max = 10
        [ diagramaprocessoList: Diagramaprocesso.list( params ) ]
    }

    def show = {
	
        def diagramaprocesso = Diagramaprocesso.get( params.id )
		
        if(!diagramaprocesso) {
            flash.message = "Diagramaprocesso not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ diagramaprocesso : diagramaprocesso ] }
    }

    def delete = {
        def diagramaprocesso = Diagramaprocesso.get( params.id )
        if(diagramaprocesso) {
            diagramaprocesso.delete()
            flash.message = "Diagramaprocesso ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "Diagramaprocesso not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def diagramaprocesso = Diagramaprocesso.get( params.id )

        if(!diagramaprocesso) {
            flash.message = "Diagramaprocesso not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ diagramaprocesso : diagramaprocesso ]
        }
    }

    def update = {
        def diagramaprocesso = Diagramaprocesso.get( params.id )
        if(diagramaprocesso) {
            diagramaprocesso.properties = params
            if(!diagramaprocesso.hasErrors() && diagramaprocesso.save()) {
                flash.message = "Diagramaprocesso ${params.id} updated"
                redirect(action:show,id:diagramaprocesso.id)
            }
            else {
                render(view:'edit',model:[diagramaprocesso:diagramaprocesso])
            }
        }
        else {
            flash.message = "Diagramaprocesso not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
	
        def diagramaprocesso = new Diagramaprocesso([projeto: Projeto.get(params.id)])
        diagramaprocesso.properties = params
        return ['diagramaprocesso':diagramaprocesso]
    }

    def save = {
        def diagramaprocesso = new Diagramaprocesso(params)
        if(!diagramaprocesso.hasErrors() && diagramaprocesso.save()) {
            flash.message = "Diagramaprocesso ${diagramaprocesso.id} created"
            redirect(action:show,id:diagramaprocesso.id)
        }
        else {
            render(view:'create',model:[diagramaprocesso:diagramaprocesso])
        }
    }
}
