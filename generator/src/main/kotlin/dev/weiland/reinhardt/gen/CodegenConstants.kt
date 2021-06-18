package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.ClassName

public object CodegenConstants {

    public fun getEntityReaderClassName(modelClass: ClassName): ClassName {
        return modelClass.peerClass(modelClass.simpleName + "EntityR")
    }

    public fun getEntityInterfaceClassName(modelClass: ClassName): ClassName {
        return modelClass.peerClass(modelClass.simpleName + "Entity")
    }

    public fun getIdFieldName(modelClass: ClassName, targetModelClass: ClassName, originalFieldName: String): String {
        return originalFieldName + "Id"
    }

    public const val ENTITY_READER_FUN: String = "entityReader"
    public const val MODEL_COMPANION_FUN: String = "objects"

}