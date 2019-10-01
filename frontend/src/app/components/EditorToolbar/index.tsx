import * as React from 'react';

import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';
import Select from 'antd/lib/select';
import 'antd/lib/select/style/css';
import Row from 'antd/lib/row';
import 'antd/lib/row/style/css';
import Col from 'antd/lib/col';
import 'antd/lib/col/style/css';
import Icon from 'antd/lib/icon';
import 'antd/lib/icon/style/css';
import Pagination from 'antd/lib/pagination';
import 'antd/lib/pagination/style/css';

const { Option } = Select;
const { Header } = Layout;

export class EditorToolbar extends React.Component<{}> {
    public render() {
        return (
            <Layout className="editorToolbar">
                <Header>
                    <Row>
                      <Col xs={24} lg={7}>
                          <Icon type='file-text' />
                          <span>&nbsp;</span>
                            <Select
                                showSearch
                                style={{ width: 160 }}
                                placeholder="Select a document"
                                optionFilterProp="children"
                                defaultActiveFirstOption={true}>
                                <Option value="1">readme.txt</Option>
                                <Option value="2">intro.txt</Option>
                                <Option value="3">doc.txt</Option>
                        </Select>
                        </Col>
                        <Col xs={24} sm={24} md={10} lg={7} xl={6}>
                            <Icon type='global' />
                            <span className='smLabel'>SOURCE </span>
                            <Select
                                    showSearch
                                    style={{ width: 160 }}
                                    placeholder="Select a language"
                                    optionFilterProp="children"
                                    defaultActiveFirstOption={true}>
                                <Option value="1">English</Option>
                                <Option value="2">Japanese</Option>
                                <Option value="3">Russian</Option>
                            </Select>
                        </Col>
                        <Col xs={24} sm={24} md={10} lg={7} xl={6}>
                            <span className="smLabel">TARGET </span>
                            <Select
                                    showSearch
                                    style={{ width: 160 }}
                                    placeholder="Select a language"
                                    optionFilterProp="children"
                                    defaultActiveFirstOption={true}>
                                <Option value="1">Japanese</Option>
                                <Option value="2">Russian</Option>
                                <Option value="3">Spanish</Option>
                            </Select>
                        </Col>
                        <span className='editorPagination'>
                         <Pagination simple defaultCurrent={1} total={50} />
                        </span>
                    </Row>
                </Header>
            </Layout>
        )
    }
}
