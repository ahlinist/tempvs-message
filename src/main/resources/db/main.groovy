databaseChangeLog {
  changeSet(id: '1538920134356-1', author: 'albvs (generated)') {
    createSequence(sequenceName: 'hibernate_sequence')
  }

  changeSet(id: '1538920134356-2', author: 'albvs (generated)') {
    createTable(tableName: 'conversation') {
      column(name: 'id', type: 'BIGINT') {
        constraints(primaryKey: true, primaryKeyName: 'conversation_pkey')
      }
      column(name: 'created_date', type: 'TIMESTAMP WITHOUT TIME ZONE')
      column(name: 'name', type: 'VARCHAR(255)')
      column(name: 'admin_id', type: 'BIGINT')
      column(name: 'last_message_id', type: 'BIGINT')
    }
  }

  changeSet(id: '1538920134356-3', author: 'albvs (generated)') {
    createTable(tableName: 'conversation_messages') {
      column(name: 'conversation_id', type: 'BIGINT') {
        constraints(nullable: false)
      }
      column(name: 'messages_id', type: 'BIGINT') {
        constraints(nullable: false)
      }
    }
  }

  changeSet(id: '1538920134356-4', author: 'albvs (generated)') {
    createTable(tableName: 'conversation_participants') {
      column(name: 'conversations_id', type: 'BIGINT') {
        constraints(primaryKey: true, primaryKeyName: 'conversation_participants_pkey')
      }
      column(name: 'participants_id', type: 'BIGINT') {
        constraints(primaryKey: true, primaryKeyName: 'conversation_participants_pkey')
      }
    }
  }

  changeSet(id: '1538920134356-5', author: 'albvs (generated)') {
    createTable(tableName: 'flyway_schema_history') {
      column(name: 'installed_rank', type: 'INTEGER') {
        constraints(primaryKey: true, primaryKeyName: 'flyway_schema_history_pk')
      }
      column(name: 'version', type: 'VARCHAR(50)')
      column(name: 'description', type: 'VARCHAR(200)') {
        constraints(nullable: false)
      }
      column(name: 'type', type: 'VARCHAR(20)') {
        constraints(nullable: false)
      }
      column(name: 'script', type: 'VARCHAR(1000)') {
        constraints(nullable: false)
      }
      column(name: 'checksum', type: 'INTEGER')
      column(name: 'installed_by', type: 'VARCHAR(100)') {
        constraints(nullable: false)
      }
      column(name: 'installed_on', type: 'TIMESTAMP WITHOUT TIME ZONE', defaultValueComputed: 'now()') {
        constraints(nullable: false)
      }
      column(name: 'execution_time', type: 'INTEGER') {
        constraints(nullable: false)
      }
      column(name: 'success', type: 'BOOLEAN') {
        constraints(nullable: false)
      }
    }
  }

  changeSet(id: '1538920134356-6', author: 'albvs (generated)') {
    createTable(tableName: 'message') {
      column(name: 'id', type: 'BIGINT') {
        constraints(primaryKey: true, primaryKeyName: 'message_pkey')
      }
      column(name: 'created_date', type: 'TIMESTAMP WITHOUT TIME ZONE')
      column(name: 'is_system', type: 'BOOLEAN')
      column(name: 'text', type: 'VARCHAR(255)')
      column(name: 'author_id', type: 'BIGINT') {
        constraints(nullable: false)
      }
      column(name: 'conversation_id', type: 'BIGINT') {
        constraints(nullable: false)
      }
      column(name: 'subject_id', type: 'BIGINT')
    }
  }

  changeSet(id: '1538920134356-7', author: 'albvs (generated)') {
    createTable(tableName: 'message_new_for') {
      column(name: 'message_id', type: 'BIGINT') {
        constraints(primaryKey: true, primaryKeyName: 'message_new_for_pkey')
      }
      column(name: 'new_for_id', type: 'BIGINT') {
        constraints(primaryKey: true, primaryKeyName: 'message_new_for_pkey')
      }
    }
  }

  changeSet(id: '1538920134356-8', author: 'albvs (generated)') {
    createTable(tableName: 'participant') {
      column(name: 'id', type: 'BIGINT') {
        constraints(primaryKey: true, primaryKeyName: 'participant_pkey')
      }
      column(name: 'created_date', type: 'TIMESTAMP WITHOUT TIME ZONE')
    }
  }

  changeSet(id: '1538920134356-9', author: 'albvs (generated)') {
    addUniqueConstraint(columnNames: 'messages_id', constraintName: 'uk_163cga9b82cbl0obssg58gkvm', tableName: 'conversation_messages')
  }

  changeSet(id: '1538920134356-10', author: 'albvs (generated)') {
    createIndex(indexName: 'flyway_schema_history_s_idx', tableName: 'flyway_schema_history') {
      column(name: 'success')
    }
  }

  changeSet(id: '1538920134356-11', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'admin_id', baseTableName: 'conversation', constraintName: 'fk6acvhq2obstf3p05xnwefer58', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'participant', validate: true)
  }

  changeSet(id: '1538920134356-12', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'subject_id', baseTableName: 'message', constraintName: 'fk6q5mk8iu92f785knvkt8101fs', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'participant', validate: true)
  }

  changeSet(id: '1538920134356-13', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'conversation_id', baseTableName: 'message', constraintName: 'fk6yskk3hxw5sklwgi25y6d5u1l', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'conversation', validate: true)
  }

  changeSet(id: '1538920134356-14', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'participants_id', baseTableName: 'conversation_participants', constraintName: 'fk8l5rmj9qli0xc7s7esnhb1wag', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'participant', validate: true)
  }

  changeSet(id: '1538920134356-15', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'message_id', baseTableName: 'message_new_for', constraintName: 'fkbx2078jgnc8qpnyie2hs8lxxk', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'message', validate: true)
  }

  changeSet(id: '1538920134356-16', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'conversations_id', baseTableName: 'conversation_participants', constraintName: 'fkh5y0aiuqqe8mqpwrj74s025w', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'conversation', validate: true)
  }

  changeSet(id: '1538920134356-17', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'new_for_id', baseTableName: 'message_new_for', constraintName: 'fklhq9pc1151mumxii25g12uxvk', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'participant', validate: true)
  }

  changeSet(id: '1538920134356-18', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'conversation_id', baseTableName: 'conversation_messages', constraintName: 'fkowwk6j0v3ydi001gu4m5lb39d', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'conversation', validate: true)
  }

  changeSet(id: '1538920134356-19', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'author_id', baseTableName: 'message', constraintName: 'fkrhnlglarcehf9y79wd9q0m438', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'participant', validate: true)
  }

  changeSet(id: '1538920134356-20', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'last_message_id', baseTableName: 'conversation', constraintName: 'fksm3966podppo987o2etdjci1r', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'message', validate: true)
  }

  changeSet(id: '1538920134356-21', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'messages_id', baseTableName: 'conversation_messages', constraintName: 'fksrkwqqihpmo60bbny5xibdx3r', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'message', validate: true)
  }

}
