databaseChangeLog {
  changeSet(id: '1538953898453-1', author: 'albvs (generated)') {
    createSequence(sequenceName: 'hibernate_sequence')
  }

  changeSet(id: '1538953898453-2', author: 'albvs (generated)') {
    createTable(tableName: 'conversation') {
      column(name: 'id', type: 'BIGINT', autoIncrement: true) {
        constraints(primaryKey: true, primaryKeyName: 'conversationPK')
      }
      column(name: 'created_date', type: 'TIMESTAMP WITHOUT TIME ZONE')
      column(name: 'name', type: 'VARCHAR(255)')
      column(name: 'type', type: 'INTEGER')
      column(name: 'admin_id', type: 'BIGINT')
      column(name: 'last_message_id', type: 'BIGINT')
    }
  }

  changeSet(id: '1538953898453-3', author: 'albvs (generated)') {
    createTable(tableName: 'conversation_messages') {
      column(name: 'conversation_id', type: 'BIGINT') {
        constraints(nullable: false)
      }
      column(name: 'messages_id', type: 'BIGINT') {
        constraints(nullable: false)
      }
    }
  }

  changeSet(id: '1538953898453-4', author: 'albvs (generated)') {
    createTable(tableName: 'conversation_participants') {
      column(name: 'conversations_id', type: 'BIGINT') {
        constraints(primaryKey: true)
      }
      column(name: 'participants_id', type: 'BIGINT') {
        constraints(primaryKey: true)
      }
    }
  }

  changeSet(id: '1538953898453-5', author: 'albvs (generated)') {
    createTable(tableName: 'message') {
      column(name: 'id', type: 'BIGINT', autoIncrement: true) {
        constraints(primaryKey: true, primaryKeyName: 'messagePK')
      }
      column(name: 'created_date', type: 'TIMESTAMP WITHOUT TIME ZONE')
      column(name: 'is_system', type: 'BOOLEAN')
      column(name: 'text', type: 'VARCHAR(255)')
      column(name: 'author_id', type: 'BIGINT')
      column(name: 'conversation_id', type: 'BIGINT')
      column(name: 'subject_id', type: 'BIGINT')
    }
  }

  changeSet(id: '1538953898453-6', author: 'albvs (generated)') {
    createTable(tableName: 'message_new_for') {
      column(name: 'message_id', type: 'BIGINT') {
        constraints(primaryKey: true)
      }
      column(name: 'new_for_id', type: 'BIGINT') {
        constraints(primaryKey: true)
      }
    }
  }

  changeSet(id: '1538953898453-7', author: 'albvs (generated)') {
    createTable(tableName: 'participant') {
      column(name: 'id', type: 'BIGINT') {
        constraints(primaryKey: true, primaryKeyName: 'participantPK')
      }
      column(name: 'created_date', type: 'TIMESTAMP WITHOUT TIME ZONE')
    }
  }

  changeSet(id: '1538953898453-8', author: 'albvs (generated)') {
    addUniqueConstraint(columnNames: 'messages_id', constraintName: 'UK_163cga9b82cbl0obssg58gkvm', tableName: 'conversation_messages')
  }

  changeSet(id: '1538953898453-9', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'admin_id', baseTableName: 'conversation', constraintName: 'FK6acvhq2obstf3p05xnwefer58', deferrable: false, initiallyDeferred: false, referencedColumnNames: 'id', referencedTableName: 'participant', validate: true)
  }

  changeSet(id: '1538953898453-10', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'subject_id', baseTableName: 'message', constraintName: 'FK6q5mk8iu92f785knvkt8101fs', deferrable: false, initiallyDeferred: false, referencedColumnNames: 'id', referencedTableName: 'participant', validate: true)
  }

  changeSet(id: '1538953898453-11', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'conversation_id', baseTableName: 'message', constraintName: 'FK6yskk3hxw5sklwgi25y6d5u1l', deferrable: false, initiallyDeferred: false, referencedColumnNames: 'id', referencedTableName: 'conversation', validate: true)
  }

  changeSet(id: '1538953898453-12', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'participants_id', baseTableName: 'conversation_participants', constraintName: 'FK8l5rmj9qli0xc7s7esnhb1wag', deferrable: false, initiallyDeferred: false, referencedColumnNames: 'id', referencedTableName: 'participant', validate: true)
  }

  changeSet(id: '1538953898453-13', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'message_id', baseTableName: 'message_new_for', constraintName: 'FKbx2078jgnc8qpnyie2hs8lxxk', deferrable: false, initiallyDeferred: false, referencedColumnNames: 'id', referencedTableName: 'message', validate: true)
  }

  changeSet(id: '1538953898453-14', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'conversations_id', baseTableName: 'conversation_participants', constraintName: 'FKh5y0aiuqqe8mqpwrj74s025w', deferrable: false, initiallyDeferred: false, referencedColumnNames: 'id', referencedTableName: 'conversation', validate: true)
  }

  changeSet(id: '1538953898453-15', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'new_for_id', baseTableName: 'message_new_for', constraintName: 'FKlhq9pc1151mumxii25g12uxvk', deferrable: false, initiallyDeferred: false, referencedColumnNames: 'id', referencedTableName: 'participant', validate: true)
  }

  changeSet(id: '1538953898453-16', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'conversation_id', baseTableName: 'conversation_messages', constraintName: 'FKowwk6j0v3ydi001gu4m5lb39d', deferrable: false, initiallyDeferred: false, referencedColumnNames: 'id', referencedTableName: 'conversation', validate: true)
  }

  changeSet(id: '1538953898453-17', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'author_id', baseTableName: 'message', constraintName: 'FKrhnlglarcehf9y79wd9q0m438', deferrable: false, initiallyDeferred: false, referencedColumnNames: 'id', referencedTableName: 'participant', validate: true)
  }

  changeSet(id: '1538953898453-18', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'last_message_id', baseTableName: 'conversation', constraintName: 'FKsm3966podppo987o2etdjci1r', deferrable: false, initiallyDeferred: false, referencedColumnNames: 'id', referencedTableName: 'message', validate: true)
  }

  changeSet(id: '1538953898453-19', author: 'albvs (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'messages_id', baseTableName: 'conversation_messages', constraintName: 'FKsrkwqqihpmo60bbny5xibdx3r', deferrable: false, initiallyDeferred: false, referencedColumnNames: 'id', referencedTableName: 'message', validate: true)
  }

}
